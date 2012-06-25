function jobId = doDeconvReg( a, b )

cluster = parcluster('local');
job = createJob(cluster);
createTask(job, @deconv_reg, 1, {a,b});
submit(job);
jobId = job.ID;

end