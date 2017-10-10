import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PairCount {

	public static class MapperClass extends Mapper<Object , Text, Text, IntWritable>{

		private final static IntWritable one = new IntWritable(1);	
		private Text word = new Text();
	
		public void map(Object  key, Text value, Context context
	                   ) throws IOException, InterruptedException {
		   	String[] tokens = value.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
	       	for(int i=0; i < tokens.length - 1; i++){
	       		for(int j=i+1; j< tokens.length; j++){
	       			if(tokens[i].compareTo("") != 0 && tokens[j].compareTo("") != 0){
		       			word.set(tokens[i] + " " + tokens[j]);
		       			context.write(word, one);
	       			}
	       		}
	       	}
	    }
	}

	public static class ReducerClass extends Reducer<Text,IntWritable,Text,IntWritable> {
		
		private IntWritable result = new IntWritable();
	
		public void reduce(Text key, Iterable<IntWritable> values,
	                      Context context
	                      ) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Paircount");
		job.setJarByClass(PairCount.class);
		job.setMapperClass(MapperClass.class);
		job.setReducerClass(ReducerClass.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.setInputDirRecursive(job, true);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
