function jobId = doConv( a, b )

cluster = parcluster('local');
job = createJob(cluster);
createTask(job, @conv_cut, 1, {a,b});
submit(job);
jobId = job.ID;

end