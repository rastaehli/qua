package com.expd.tools;

import java.util.ArrayList;
import java.util.List;

public class PipeGen {
    private List<JobSpec> jobs;

    public PipeGen() {
        jobs = new ArrayList<>();
    }

    public void addVersionJob(String increment_minor) {

    }

    public void addCompileJob(String compile) {
        jobs.add(new JobSpec("maven_compile", "compile"));
    }

    public void addTestJob(String unit_test) {
    }

    public void addPackageJob(String aPackage) {
    }

    public void addDeployJob(String deploy_qa1) {
    }

    public String generateGitlabCiYml() {
        StringBuilder sb = new StringBuilder();
        for (JobSpec j: jobs) {
            j.append(sb);
        }
        return sb.toString();
    }
}
