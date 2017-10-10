import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class LemmetizationCoOccurence {
	
	private static Map<String, Set<String>> LemmaMap = new HashMap<String, Set<String>>();

	public static class MapperClass extends Mapper<LongWritable , Text, Text, Text>{
		
		private static final int FLUSH_SIZE = 2000;
		private static Map<String, String> locationLemmaMap = new HashMap<String, String>();
		
		public void map(LongWritable  key, Text value, Context context) throws IOException, InterruptedException {
			String location, line;
			Set<String> lemmaWordSet = new HashSet<String>();
			Set<String> lemmaNeighborSet = new HashSet<String>();
			if(value.toString().compareTo("") != 0){
				location = value.toString().split(">")[0];
				line = value.toString().split(">")[1].trim().replaceAll("[^a-zA-Z0-9\\s]", "");
				location = location + ">";
				String[] tokens = line.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
				if (tokens.length > 1) {
					for(int i=0; i < tokens.length - 1; i++){
						for(int j=i+1; j< tokens.length; j++){
							if(tokens[i].compareTo("") != 0 && tokens[j].compareTo("") != 0){	
								String wordText = tokens[i].replaceAll("j","i").replaceAll("v","u");
								String neighborText = tokens[j].replaceAll("j","i").replaceAll("v","u");
								if(LemmaMap.containsKey(wordText)){
									lemmaWordSet = LemmaMap.get(wordText);
								}else{
									lemmaWordSet = new HashSet<String>();
									lemmaWordSet.add(wordText);
								}
								if(LemmaMap.containsKey(neighborText)){
									lemmaNeighborSet = LemmaMap.get(neighborText);
								}else{
									lemmaNeighborSet = new HashSet<String>();
									lemmaNeighborSet.add(neighborText);
								}
								for(String lemma1 : lemmaWordSet){
									for(String lemma2 : lemmaNeighborSet){
										if(locationLemmaMap.containsKey(lemma1 + " " + lemma2)){
											locationLemmaMap.put(lemma1 + " " + lemma2, locationLemmaMap.get(lemma1 + " " + lemma2) + "," + location);
										}else{
											locationLemmaMap.put(lemma1 + " " + lemma2, location);
										}
									}
								}
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
		Job job = Job.getInstance(conf, "LemmetizationOccurence");
		job.setJarByClass(LemmetizationCoOccurence.class);
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