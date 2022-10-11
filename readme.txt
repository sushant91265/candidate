Please document here:
* Classes you have implemented or modified, including test classes
1) NaiveJobRunnerTests => This class is used test the JobRunnerImpl, instead of NaiveJobRunner.

2) JobRunnable => This class acts as a wrapper class for Job, which defines the Job and its priority mapping; where priority is the summation of the duration of the Job with same customerId.

3) JobRunnerImpl => This class is an implementation class for the JobRunner, where we execute the jobs based on the faireness criteria. The highest priority job will be submitted for execution first. The process leverages a min heap that keeps track of the next job with the highest priority.



* Any assumptions that affected your design
- No infinite jobs, i.e. job will complete at some point in time.


* Any shortcomings of your implementation
- Current implementation can suffer from long running jobs.
- Tests varies from machine to machine.
- We could modularize the code, by having multiple ways to calculate the priority.
- Additional debug loggers can be addded.


* An explanation of your definition of fairness execution
- Each customer should get around the same amount of execution time.
- We are achieving this by summing up the duration of Jobs for each customerId and then sorting jobs based on the sum.