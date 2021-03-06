package mapreduce.file2hbase;

import java.io.IOException;
//import java.util.Vector;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mapreduce.type.RectangleWritableComparable;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HTable;
//import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import util.Transformer;
import util.grid.Grid;
import util.pf.sensor.laser.LaserSensor;

public class Reducer2Hbase 
	extends Reducer<IntWritable, RectangleWritableComparable, ImmutableBytesWritable, Put>{
	private HTable Table = null;
	//private Vector<Put> Puts = null;
	private byte[] Family_Distance = null;
	private byte[] Family_Energy = null;
	
	public enum Counters {
		A,
		B,
		C,
		D,
		E,
		F,
		G,
		H,
		I,
		J,
		K,
		PUT
	}
	
	@Override
	protected void setup(Context context) 
			throws IOException, InterruptedException {
		
		Table = new HTable(context.getConfiguration(), context.getConfiguration().get("conf.table","map"));
		//Puts = new Vector<Put>();
		Family_Distance = Bytes.toBytes(context.getConfiguration().get("conf.family.distance","distance"));
		Family_Energy = Bytes.toBytes(context.getConfiguration().get("conf.family.energy","energy"));
//		Family_X = Bytes.toBytes(context.getConfiguration().get("conf.family.laserpoint.x","laserpoint.x"));
//		Family_Y = Bytes.toBytes(context.getConfiguration().get("conf.family.laserpoint.y","laserpoint.y"));
		
		
	}

	
	
	public void reduce(IntWritable key, Iterable<RectangleWritableComparable> values, Context context)
			throws IOException, InterruptedException {
		System.out.println("server name: " + java.net.InetAddress.getLocalHost().getHostName());
		System.out.println("key = "+key.get());
		try {
			System.out.println("part= "+context.getPartitionerClass().toString());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.println("reucde number = "+context.getNumReduceTasks());
		
		String path = context.getConfiguration().get("conf.input", "/user/eeuser/map1024.jpeg");
		int orientation = context.getConfiguration().getInt("conf.orientation",4);
		List<Put> puts = new ArrayList<Put>();
		
		for (RectangleWritableComparable value: values) {
			try {
				context.getCounter(Counters.A).increment(1);
				LaserSensor laserConfig = new LaserSensor();
				laserConfig.angle_min = -90;
				laserConfig.angle_max = 90;
				laserConfig.angle_resolution = Math.round(360/orientation);
				laserConfig.range_min = 0f;
				laserConfig.range_max = -1;
				Grid gridmap = new Grid(/*orientation, (orientation/2)+1, -1,*/ path);
				gridmap.laser.setupSensor(laserConfig);
//				assert(gridmap.orientation==gridmap.laser.getOrientation());
				gridmap.readMapImageFromHadoop(path, context);
				//TODO if there is range_max of laser beam, replace -1 with range_max.
				gridmap.pre_compute(value.x.get(), value.y.get(), value.width.get(), value.height.get()/*, -1*/);
				context.getCounter(Counters.A).increment(1);
				//int rowkey;
				String row_str = new String();
				Random random = new Random();
				int translateX = value.x.get();
				int translateY = value.y.get();
				for (int i = 0; i < value.width.get(); i++) {
					for (int j = 0; j < value.height.get(); j++) {
						
						// transform to absolute type
						row_str = Transformer.xy2RowkeyString(i+translateX, j+translateY, random);
						context.getCounter(Counters.PUT).increment(1);
						Put put = new Put(Bytes.toBytes(row_str));
						
						// have been improved to keep the better efficiency
						//put.setWriteToWAL(false);
						put.setDurability(Durability.SKIP_WAL);
						
						for (int k = 0; k < gridmap.laser.getOrientation(); k++) {

							context.getCounter(Counters.D).increment(1);
							
							String measurements = String.valueOf(gridmap.G[i][j].circle_measurements.get(k));
							
							put.add(Family_Distance,
									Bytes.toBytes(String.valueOf(k)),
									Bytes.toBytes(measurements));
							
							context.getCounter(Counters.E).increment(1);
							
							
							String energy = String.valueOf(gridmap.G[i][j].getEnergy(k));
							Put putEnergy = new Put(Bytes.toBytes(energy));
							//putEnergy.setWriteToWAL(false);
							put.setDurability(Durability.SKIP_WAL);
							putEnergy.add(Family_Energy, 
									Bytes.toBytes(row_str),
									Bytes.toBytes(String.valueOf(k)));
							
							puts.add(putEnergy);
							
							context.getCounter(Counters.F).increment(1);
							
							/*String laser_x = String.valueOf(gridmap.G[i][j].measurement_points[k].x);
							
							put.add(Family_X,
									Bytes.toBytes(String.valueOf(k)),
									Bytes.toBytes(laser_x));
					
							context.getCounter(Counters.G).increment(1);
							
							String laser_y = String.valueOf(gridmap.G[i][j].measurement_points[k].y);
							
							put.add(Family_Y,
									Bytes.toBytes(String.valueOf(k)),
									Bytes.toBytes(laser_y));*/
							//TODO separate the load
							Table.put(putEnergy);
							Table.put(put);
						}
						
						
						
						context.getCounter(Counters.H).increment(1);
						puts.add(put);
						
						context.getCounter(Counters.I).increment(1);
					}
				}
				System.out.println("last key: "+row_str.toString());
				//TODO close grid map
				gridmap.close();
			} catch (Exception e) {
				context.getCounter(Counters.J).increment(1);
				e.printStackTrace();
			}
		}
		context.getCounter(Counters.K).increment(1);
		//TODO separate the load
		//Table.put(puts);
		
	}

	@Override
	protected void cleanup(Context context)
			throws IOException, InterruptedException {
		//Table.flushCommits();
		Table.close();
		super.cleanup(context);
	}
	

}
