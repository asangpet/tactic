function [state,data] = getJobStatus( jobID )

cluster = parcluster('local');
job = findJob(cluster,'ID',jobID);
data = [];

if isempty(job)
    state = -1;
    return
end

if strcmp(job.State,'finished')
    data = job.fetchOutputs;
    state = 1;
else
    state = 0;
end

end