package org.acm.rstaehli.qua;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acm.rstaehli.qua.Description.MATCH_ANY;

public class ListBuilder extends AbstractPassiveServiceBuilder {

    private static final Logger logger = Logger.getLogger(ListBuilder.class);

    // these are the Description properties supported by this builder
    public static final String SHARED_DESCRIPTION = "sharedDescription";  // if provided, created items inherit type and properties from this description
    public static final String ITEM_ID_PROPERTY = "itemIdProperty"; // if ITEM_IDS provided, created items set this property with id value from ITEM_IDS
    public static final String ITEM_IDS = "itemIds"; // the unique ITEM_ID_PROPERTY values for each created list item description
    public static final String ITEMS = "items";  // list item descriptions; no need to provide ITEM_IDS if items are provided this way
    public static final String CHILD_LISTS = "childLists";  // nested lists support hierarchical inheritance of ITEM_PROPERTIES

    public static final String ID = "id";  // default property to set with list item identity value

    private static final List<Description> EMPTY_DESCRIPTION_LIST = new ArrayList<>();

    protected Repository repo;

    public static Map<String, Object> matchProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(SHARED_DESCRIPTION, MATCH_ANY);
        props.put(ITEM_ID_PROPERTY, MATCH_ANY);
        props.put(ITEM_IDS, MATCH_ANY);
        props.put(ITEMS, MATCH_ANY);
        props.put(CHILD_LISTS, MATCH_ANY);
        return props;
    }

    public ListBuilder(Repository repo) {
        this.repo = repo;
    }

    public List<Description> childLists(Description list) {
        if (list.hasProperty(CHILD_LISTS)) {
            return list.listDescriptionProperty(CHILD_LISTS);
        }
        return EMPTY_DESCRIPTION_LIST;
    }

    public List<String> itemIds(Description list) {
        if (list.hasProperty(ITEM_IDS)) {
            return (List<String>)list.properties().get(ITEM_IDS);
        }
        return new ArrayList<>();
    }

    public List<Description> items(Description list) {
        if (list.hasProperty(ITEMS)) {
            return list.listDescriptionProperty(ITEMS);
        }
        return EMPTY_DESCRIPTION_LIST;
    }

   /**
     * build a collection of Descriptions based on "childType" and this sharedContext as a member of a "sharedContexts" property
     * @param list
     */
    @Override
    public void assemble(Description list) {
        Map<String, Object> props = list.properties();
        List<Description> items = new ArrayList<>();

        Description sharedDescription = list.hasProperty(SHARED_DESCRIPTION)
                ? list.descriptionProperty(SHARED_DESCRIPTION)
                : new Description();

        // get items from nested lists
        for (Description childList: childLists(list)) {
            try {
                inheritListProperties( list, sharedDescription, childList );
                items.addAll((List<Description>)childList.service(repo));
            } catch (Exception e) {
                logger.error("Exception while assembling childList: " + e);
                e.printStackTrace();
            }
        }

        // get items from enumerated item ids
        for (String itemId: itemIds(list)) {
            try {
                Description itemDesc = new Description(sharedDescription.type());  // build child description
                if (list.hasProperty(ITEM_ID_PROPERTY)) {
                    itemDesc.properties().put(list.stringProperty(ITEM_ID_PROPERTY), itemId);
                } else {
                    itemDesc.properties().put(ID, itemId);
                }
                Description.copyMappings(sharedDescription.properties(), itemDesc.properties());  // inherit from shared description
                items.add((Description)itemDesc.assemble(repo));
            } catch (Exception e) {
                logger.error("Exception while constructing item from id: " + e);
                e.printStackTrace();
            }
        }
        for (Description itemDesc: items(list)) {
            try {
                Description.copyMappings(sharedDescription.properties(), itemDesc.properties());  // inherit from sharedDescription
                items.add(itemDesc);
            } catch (Exception e) {
                logger.error("Exception while constructing item from id: " + e);
                e.printStackTrace();
            }
        }
        list.setServiceObject(items);
    }

    private void inheritListProperties(Description list, Description sharedDescription, Description child ) {
        if (!child.hasProperty(SHARED_DESCRIPTION)) {
            child.properties().put(SHARED_DESCRIPTION, sharedDescription);
        }
        Description childSharedDescription = child.descriptionProperty(SHARED_DESCRIPTION);
        if (sharedDescription.type() != null && !childSharedDescription.isTyped()) {
            childSharedDescription.setType(sharedDescription.type());
        }
        copyMappingsRecursively(sharedDescription.properties(), childSharedDescription.properties());  // inherit from sharedDescription
        if (list.hasProperty(ITEM_ID_PROPERTY) && !child.hasProperty(ITEM_ID_PROPERTY)) {
                child.properties().put(ITEM_ID_PROPERTY, list.properties().get(ITEM_ID_PROPERTY));
        }
    }

    public static void copyMappingsRecursively(Map<String,Object> source, Map<String, Object> target) {
        if (source == null) {
            return;
        }
        for (String key: source.keySet()) {
            if (!target.containsKey(key)) {  // don't override existing mappings
                target.put(key,source.get(key));
            } else if (target.get(key) instanceof Description && source.get(key) instanceof Description) {
                // unless both are descriptions, in which case, copy mappings for the description properties
                Description sourceDesc = (Description)source.get(key);
                Description targetDesc = (Description)target.get(key);
                if (sourceDesc.type().equals(targetDesc.type())) {
                    copyMappingsRecursively(sourceDesc.properties(), targetDesc.properties());
                }
            }
        }
    }

}
