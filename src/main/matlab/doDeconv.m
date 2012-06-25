function jobId = doDeconv( a, b )

cluster = parcluster('local');
job = createJob(cluster);
createTask(job, @deconv_master, 2, {a,b});
submit(job);
jobId = job.ID;

end