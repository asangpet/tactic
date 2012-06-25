function jobId = doDeconvWnr( a, b )

cluster = parcluster('local');
job = createJob(cluster);
createTask(job, @deconv_wnr, 1, {a,b});
submit(job);
jobId = job.ID;

end