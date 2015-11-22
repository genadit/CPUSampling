/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.kenshoo.nps;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale.Builder;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;

/**
 *
 * @author genadit
 */
public class CreateNPS {

	public void createNPSFile(StackTraceSnapshotBuilder builder, String path)
			throws CPUResultsSnapshot.NoDataAvailableException, FileNotFoundException, IOException {

		CPUResultsSnapshot snapshot;

		snapshot = builder.createSnapshot(System.currentTimeMillis());

		LoadedSnapshot ls = new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);

		File file = new File(path + "/test.nps");
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		ls.save(dos);
		ls.setFile(file);
		ls.setSaved(true);

		System.out.println("Snapshot saved!");

	}

}
