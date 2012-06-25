function deleteJob( jobID )

cluster = parcluster('local');
job = findJob(cluster,'ID',jobID);
if (isempty(job))
    return;
end
delete(job);

end