import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class StripeCount {
	
	public static class MapperClass extends Mapper<LongWritable,Text,Text,MapWritable> {
		
		private MapWritable occurrenceMap = new MapWritable();
		private Text word = new Text();
		
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String[] tokens = value.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
			if (tokens.length > 1) {
				for (int i = 0; i < tokens.length - 1; i++) {
					word.set(tokens[i]);
					occurrenceMap.clear();
					for(int j = i+1; j < tokens.length; j++) {
						if(tokens[i].compareTo("") != 0 && tokens[j].compareTo("") != 0){
							Text neighbor = new Text(tokens[j]);
							if(occurrenceMap.containsKey(neighbor)){
								IntWritable count = (IntWritable)occurrenceMap.get(neighbor);
								count.set(count.get()+1);
								occurrenceMap.put(neighbor,count);
							}else{
								occurrenceMap.put(neighbor,new IntWritable(1));
							}
						}
					}
					context.write(word,occurrenceMap);
				}
			}
		}
	}

	public static class ReducerClass extends Reducer<Text, MapWritable, Text, Text> {

	    private MapWritable incrementingMap = new MapWritable();

	    @Override
	    protected void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException, InterruptedException {
	        incrementingMap.clear();
	        for (MapWritable value : values) {
	            addAll(value);
	        }
	        for(Object objkey : incrementingMap.keySet()){
	            context.write(key,new Text(objkey + " " + incrementingMap.get(objkey)));
	    	}
	    }

	    private void addAll(MapWritable mapWritable) {
	        Set<Writable> keys = mapWritable.keySet();
	        for (Writable key : keys) {
	            IntWritable fromCount = (IntWritable) mapWritable.get(key);
	            if (incrementingMap.containsKey(key)) {
	                IntWritable count = (IntWritable) incrementingMap.get(key);
	                count.set(count.get() + fromCount.get());
	                incrementingMap.put(key, count);
	            } else {
	                incrementingMap.put(key, fromCount);
	            }
	        }
	    }
	}

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Stripecount");
		job.setJarByClass(StripeCount.class);
		job.setMapperClass(MapperClass.class);
		job.setReducerClass(ReducerClass.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.setInputDirRecursive(job, true);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}