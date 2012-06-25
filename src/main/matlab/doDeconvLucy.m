function jobId = doDeconvLucy( a, b )

cluster = parcluster('local');
job = createJob(cluster);
createTask(job, @deconv_lucy, 1, {a,b});
submit(job);
jobId = job.ID;

end