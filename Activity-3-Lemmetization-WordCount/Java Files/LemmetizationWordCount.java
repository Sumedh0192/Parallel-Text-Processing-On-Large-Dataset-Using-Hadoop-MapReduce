import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class LemmetizationWordCount {
	
	private static Map<String, Set<String>> LemmaMap = new HashMap<String, Set<String>>();

	public static class MapperClass extends Mapper<LongWritable , Text, Text, Text>{
		
		private static final int FLUSH_SIZE = 2000;
		private static Map<String, String> locationLemmaMap = new HashMap<String, String>();
		
		public void map(LongWritable  key, Text value, Context context) throws IOException, InterruptedException {
			String location, line;
			if(value.toString().compareTo("") != 0){
				location = value.toString().split(">")[0];
				line = value.toString().split(">")[1].trim().replaceAll("[^a-zA-Z0-9\\s]", "");
				location = location + ">";
				StringTokenizer itr = new StringTokenizer(line);
				String wordText = "";
				while (itr.hasMoreTokens()) {
					wordText = itr.nextToken();
					if(wordText.compareTo("") != 0){
						wordText = wordText.replaceAll("j","i").replaceAll("v","u");
						if(LemmaMap.containsKey(wordText)){
							for(String lemma : LemmaMap.get(wordText)){
								if(locationLemmaMap.containsKey(lemma)){
									locationLemmaMap.put(lemma, locationLemmaMap.get(lemma) + "," + location);
								}else{
									locationLemmaMap.put(lemma, location);
								}
							}
						}else{
							if(locationLemmaMap.containsKey(wordText)){
								locationLemmaMap.put(wordText, locationLemmaMap.get(wordText) + "," + location);
							}else{
								locationLemmaMap.put(wordText, location);
							}
						}
					}
				}
			}
			flushMap(context, false);
		}
		
		private void flushMap(Context context, boolean force) throws IOException, InterruptedException {
			if(!force) {
				int size = locationLemmaMap.size();
			if(size < FLUSH_SIZE)
				return;
			}
			for(String lemma : locationLemmaMap.keySet()){
				context.write(new Text(lemma), new Text(locationLemmaMap.get(lemma)));
			}
			locationLemmaMap.clear(); 
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
			flushMap(context, true); 
		}
	}

	public static class ReducerClass extends Reducer<Text,Text,Text,Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context
						   ) throws IOException, InterruptedException {
			String locationString = "";
			for (Text val : values) {
				locationString += val.toString() + ", ";
			}
			int count = locationString.length() - locationString.replace(">", "").length();
			locationString = "{" + locationString.substring(0, locationString.length() - 2) + "}{count:" + String.valueOf(count) + "}";
			context.write(key, new Text(locationString));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "LemmetizationWordCount");
		job.setJarByClass(LemmetizationWordCount.class);
		job.setMapperClass(MapperClass.class);
		job.setReducerClass(ReducerClass.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileInputFormat.setInputDirRecursive(job, true);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		String[] words;
		BufferedReader br = new BufferedReader(new FileReader(args[2]));
		Set<String> LemmaSet = new HashSet<String>();
		String line;
		while((line = br.readLine()) != null){
			LemmaSet = new HashSet<String>();
			words = line.split(",");
			for(int i=1; i< words.length; i++){
				if(words[i].compareTo("") != 0){
					LemmaSet.add(words[i]);
				}
			}
			LemmaMap.put(words[0], LemmaSet);
		}
		br.close();
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}