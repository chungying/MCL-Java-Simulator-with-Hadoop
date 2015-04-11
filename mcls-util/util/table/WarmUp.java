package util.table;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.util.Bytes;

public class WarmUp extends Base{

	public static class Bytes2IntegerComparator implements Comparator<byte[]>{

		@Override
		public int compare(byte[] o1, byte[] o2) {
			if(o1==o2){
				return 0;
			}
			Double d1 = Double.parseDouble(Bytes.toString(o1));
			Double d2 = Double.parseDouble(Bytes.toString(o2));
			return d1.compareTo(d2);
		}
		
	}
	
	public static void main(String[] args){
		NavigableMap<byte[], String> map = new TreeMap<byte[], String>(new Bytes2IntegerComparator());
		for(int i = 0; i < 100 ; i++){
			map.put(Bytes.toBytes(String.valueOf(i)), String.valueOf(i));
			
		}
		for(Entry<byte[], String> e: map.entrySet()){
			System.out.println(e.getValue());
		}
	}
	
	@Override
	public void run() {
		try {
			this.setup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		try {
			this.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
