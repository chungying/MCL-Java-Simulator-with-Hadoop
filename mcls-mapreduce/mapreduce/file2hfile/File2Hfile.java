package mapreduce.file2hfile;

import mapreduce.file2hbase.File2Hbase;
import mapreduce.input.ImageSpliterInputFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;

public class File2Hfile {
	
	public static void main(String[] args) throws Exception {
		long jobTime = System.currentTimeMillis();
		String[] arg = run(args);
		jobTime = System.currentTimeMillis() - jobTime;
		
		//String command = "sudo -u hdfs hadoop fs -chown -R hbase ";
		//String output = Command.excuteCommand(command+arg[0]);
		//System.out.println(output);
		
		long loadTime = System.currentTimeMillis();
		int exit = ToolRunner.run(new LoadIncrementalHFiles(HBaseConfiguration.create()), arg);
		loadTime = System.currentTimeMillis() - loadTime;
		
		System.out.println("job time: "+ jobTime + " ms");
		System.out.println("total time:" + (loadTime) + " ms");
		System.exit(exit);
	}

	public static String[] run(String[] args) throws Exception {
		
		Configuration conf = HBaseConfiguration.create();

		// ImportFromFile-7-Args Give the command line arguments to the
		// generic parser first to handle "-Dxyz" properties.
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		CommandLine cmd = File2Hbase.parseArgs(otherArgs);
		// ^^ ImportFromFile
		// check debug flag and other options
		if (cmd.hasOption("d"))
			conf.set("conf.debug", "true");
		// get details
		// vv ImportFromFile

		String tableName = cmd.getOptionValue("t");
		String input = cmd.getOptionValue("i");
		String mappers = cmd.getOptionValue("m");
		String orientation = cmd.getOptionValue("o");
		System.out.println(input);
		conf.set("conf.input", input);
		conf.set("conf.orientation", orientation);
		conf.set("conf.table", tableName);
		conf.set("conf.family.distance", "distance");
		conf.set("conf.family.energy", "energy");
		conf.set("conf.family.laserpoint.x", "laserpoint.x");
		conf.set("conf.family.laserpoint.y", "laserpoint.y");
		
		// ImportFromFile-8-JobDef Define the job with the required classes.
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "Import from file " + input + "through " + mappers + "mapper(s)" 
		+ " into table " + tableName + " with " + orientation + " orientation.");
		// ((JobConf)job.getConfiguration()).setJar("/home/w514/iff.jar");
		job.setJarByClass(File2Hbase.class);
		
		job.getConfiguration().set(ImageSpliterInputFormat.MAP_NUMBER, mappers);
		job.setInputFormatClass(ImageSpliterInputFormat.class);
		
		ImageSpliterInputFormat.addInputPath(job, new Path(input));
		//TODOdone mapper 
		job.setMapperClass(Image2Mapper.class);
		job.setMapOutputKeyClass(ImmutableBytesWritable.class);
		job.setMapOutputValueClass(Put.class);
		job.setCombinerClass(HfileCombiner.class);
		//job.setReducerClass(HfileReducer.class);
		job.setOutputFormatClass(HFileOutputFormat2.class);
		String userName = System.getProperty("user.name");
		String outputStr = "/user/"+userName+"/hfiles/"+tableName+"/"+System.currentTimeMillis();
		FileOutputFormat.setOutputPath(job, new Path(outputStr));
		HTable hTable = new HTable(conf, tableName);
		HFileOutputFormat2.configureIncrementalLoad(job, hTable);
		
		if(!job.waitForCompletion(true)){
			System.out.println("job failed");
			System.exit(1);
		}
		hTable.close();
		
		return new String[]{outputStr, tableName};
	}
}
