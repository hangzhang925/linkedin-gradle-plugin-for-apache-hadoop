/*
 * Copyright 2015 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.linkedin.gradle.hadoopdsl.job;

/**
 * Job class for type=spark jobs.
 * <p>
 * In the DSL, a SparkJob can be specified with:
 * <pre>
 *   def flags = ['version','verbose','help']
 *   def params = ['param1','param2']
 *
 *   sparkJob('jobName') {
 *     uses 'com.linkedin.azkaban.spark.HelloSpark'  // Required
 *     executes './lib/azkaban-spark-job.jar' // Required
 *     appParams params // application parameters
 *     enableFlags flags
 *
 *     set properties: [
 *       'master' : 'yarn-cluster',
 *       'executor-memory' : '1g'
 *     ]
 *
 *     set sparkConfs: [
 *       'key1' : 'val1',
 *       'key2' : 'val2'
 *     ]
 *
 *     queue 'marathon'
 *
 *   }
 * </pre>
 */

class SparkJob extends HadoopJavaProcessJob {
  Map<String, Object> sparkConfs;
  Set<String> flags;
  List<String> appParams;
  String appClass;
  String executionJar;
  String yarnQueue;

  /**
   * Constructor for a SparkJob.
   *
   * @param jobName The job name
   */
  SparkJob(String jobName) {
    super(jobName);
    sparkConfs = new LinkedHashMap<String, Object>();
    flags = new HashSet<String>();
    appParams = new LinkedList<String>()
    setJobProperty("type", "spark");
  }

  /**
   * Clones the job.
   *
   * @return The cloned job
   */
  @Override
  SparkJob clone() {
    return clone(new SparkJob(name));
  }

  /**
   * Helper method to set the properties on a cloned job.
   *
   * @param cloneJob The job being cloned
   * @return The cloned job
   */
  SparkJob clone(SparkJob cloneJob) {
    cloneJob.properties.putAll(properties);
    cloneJob.sparkConfs.putAll(sparkConfs);
    cloneJob.flags.addAll(flags);
    cloneJob.appParams.addAll(appParams);
    cloneJob.appClass = appClass;
    cloneJob.executionJar = executionJar;
    cloneJob.yarnQueue = yarnQueue;
    return super.clone(cloneJob);
  }

  /**
   * DSL method to specify job properties to set on the job. Specifying job properties causes lines
   * of the form key=val to be written to the job file.
   * <p>
   * Additionally for JavaProcessJobs, you can specify JVM properties by using the syntax
   * "set jvmProperties: [ ... ]", which causes a line of the form
   * jvm.args=-Dkey1=val1 ... -DkeyN=valN to be written to the job file.
   * <p>
   * Additionally for HadoopJavaProcessJob subclasses, you can specify Hadoop job configuration
   * properties by using the syntax "set confProperties: [ ... ]", which causes lines of the form
   * hadoop-inject.key=val to be written to the job file.
   * <p>
   * Additionally for SparkJobs, you can specify Spark configuration by using the syntax
   * "set sparkConfs: [ ... ]". For each parameter you set, a line of the form conf.key=val will
   * be written to the job file.
   *
   * @param args Args whose key 'properties' has a map value specifying the job properties to set;
   *   or a key 'jvmProperties' with a map value that specifies the JVM properties to set;
   *   or a key 'confProperties' with a map value that specifies the Hadoop job configuration properties to set;
   *   or a key 'sparkConfs' with a map value that specifies the Spark configuration to set
   */
  @Override
  void set(Map args) {
    super.set(args);
    if (args.containsKey("sparkConfs")) {
      Map<String, Object> sparkConfs = args.sparkConfs;
      sparkConfs.each() { String name, Object value ->
        setConf(name, value);
      }
    }
  }

  /**
   * DSL setConf method sets Spark configurations for the job. When the job file is built, job
   * properties of the form conf.name=value are added to the job file.
   *
   * @param name The Spark parameter name
   * @param value The Spark parameter value
   */
  void setConf(String name, Object value) {
    sparkConfs.put(name, value);
    setJobProperty("conf.${name}", value);
  }

  /**
   * DSL enableFlags method sets the flags for the job. When the job file is build, job properties
   * of the form flag.flagName=true is added in the job file.
   * @param flags
   */
  void enableFlags(List<String> flags) {
    this.flags = flags.toSet();
    flags.each { flag ->
      setJobProperty("flag.$flag",'true');
    }
  }

  /**
   * DSL appParams method sets the application parameters for the job. When the job file is build, job properties
   * of the form params=value1 value2 value3 is added in the job file.
   * @param appParams
   */
  void appParams(List<String> appParams) {
    appParams = appParams;
    setJobProperty('params',appParams.join(" "));
  }

  /**
   * DSL method uses specifies the class to be used for the job. This method
   * causes the property class=value to be added the job. This method
   * is required to build the job.
   * @param script The Spark script for the job
   */
  @Override
  void uses(String appClass) {
    this.appClass = appClass;
    setJobProperty("class", appClass);
  }

  /**
   * DSL method executes specifies the execution-jar for the spark job. The specified value can be either
   * an absolute or relative path to the execution jar. This method causes the property execution-jar=value
   * to be added to the job. This method is required to build the job.
   * @param executionJar
   */
  void executes(String executionJar) {
    this.executionJar = executionJar;
    setJobProperty("execution-jar",executionJar);
  }

  /**
   * DSL queue method to declare the queue in which this job should run. This is handled differently in spark
   * than other jobs. This method causes the property queue=value to be added to the job.
   *
   * @param queueName The name of the queue in which this job should run
   */
  @Override
  void queue(String yarnQueue) {
    this.yarnQueue = yarnQueue;
    setJobProperty("queue", yarnQueue);
  }

}