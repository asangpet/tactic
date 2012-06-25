function waitForJob( jobID, timeout )

cluster = parcluster('local');
job = findJob(cluster,'ID',jobID);

if isempty(job)
    return
else
    wait(job, 'finished', timeout);
end

end