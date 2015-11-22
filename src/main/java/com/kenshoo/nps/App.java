/**
 * Copyright 2015 Genady Tsvik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.kenshoo.nps;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import static java.util.Calendar.MONDAY;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import static javax.management.Query.or;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.openide.util.Exceptions;

/**
 * Hello world!
 *
 */
public class App {

	static long elapsedTime = 0;
	static String path = null;

	public static void main(String[] args) throws MalformedURLException, IOException,
			CPUResultsSnapshot.NoDataAvailableException {

		if (args.length != 3) {
			System.out.println("================================================================================");
			System.out.println("Error: 3 args are required");
			System.out.println("================================================================================");
			System.out
					.println("Please insert the following parameters:\n1.Server Name\n2.Path to Save\n3.Sampling Time in Minutes");
			System.out.println("                                                                              ");
			System.out.println("for Instance: labliwb9106.kenshooprd.local /tmp 10");
		} else {

			String serverName = args[0];

			String pathToSave = args[1];
			path = pathToSave;
			String timeForSampling = args[2];
			int timeForSamplingInMinutes = Integer.parseInt(timeForSampling);
			Thread testThread = new Thread(new Runnable() {

				public void run() {

					long startTime = System.currentTimeMillis();
					while (true) {

						long elapsed = (System.currentTimeMillis() - startTime) / 1000;
						System.err.println("Kenshoo Java Profiling is running about " + elapsed + " sec");
						try {
							Thread.sleep(10000);
						} catch (InterruptedException ex) {
							Exceptions.printStackTrace(ex);
						}

					}

				}

			});

			testThread.start();
			StackTraceSnapshotBuilder builder = new StackTraceSnapshotBuilder();

			JMXServiceURL jmxUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverName + ":9853/jmxrmi");
			JMXConnector jmxConn = JMXConnectorFactory.connect(jmxUrl, null);
			MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
			ThreadMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.THREAD_MXBEAN_NAME,
					ThreadMXBean.class);

			// System.out.println(Arrays.toString(mxBean.getAllThreadIds()));
			System.out.println("The amount of threads in the system is:" + mxBean.getThreadCount());

			long startTime = System.currentTimeMillis();
			long startTimeLoop = System.currentTimeMillis();

			while (elapsedTime < timeForSamplingInMinutes) {

				ThreadInfo[] ti = mxBean.getThreadInfo(mxBean.getAllThreadIds(), Integer.MAX_VALUE);

				builder.addStacktrace(ti, System.nanoTime());

				elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

				long timeInSecoonds = (System.currentTimeMillis() - startTimeLoop) / 1000;
				// System.out.println(timeInSecoonds);

				if (timeInSecoonds >= 60) {
					System.out.println("Starting to create the NPS file ");
					System.out.println(System.currentTimeMillis() - startTimeLoop);
					runCreation(builder, path);
					System.out.println("Waiting 15 minutes to create the next NPS");
					startTimeLoop = System.currentTimeMillis();

				}

			}

			runCreation(builder, path);

			System.exit(0);
			// Thread.sleep(20);
		}
	}

	static void runCreation(StackTraceSnapshotBuilder builder, String path)
			throws CPUResultsSnapshot.NoDataAvailableException, IOException {
		new CreateNPS().createNPSFile(builder, path);

	}

}
