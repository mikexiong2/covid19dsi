package edu.columbia.dbmi.covid.covidpub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TestTTT {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONArray nodes = new JSONArray();
		String content = FileUtil.readFile(
				"/Users/cy2465/Downloads/pubmed_ref_project/litcovid_with_label_index_predicted_weighted.csv");
		String[] ars = content.split("\n");
		Set<String> includenodes = new HashSet<String>();
		List<String> selectednode = new ArrayList<String>();
		for (String a : ars) {
			// Article article=artiles.get(i);
			System.out.println(a);
			String[] aen = a.split("\t");
			if (aen.length > 5) {
				System.out.println(aen[5]);
				if(aen[5].contains("Epidemic_Forecasting")){
					JSONObject node = new JSONObject();
					if (aen.length > 2) {
						node.accumulate("name", aen[2]);
					}
					if (aen.length > 3) {
						node.accumulate("title", aen[3]);
					}
					node.accumulate("keywords", "");
					node.accumulate("group", 1);
					System.out.println(node);
					nodes.add(node);
					selectednode.add(aen[0]);
				}
			}
		}

		HashMap<String, String> iddic = new HashMap<String, String>();
		for (int i = 0; i < selectednode.size(); i++) {
			String idtrans = selectednode.get(i);
			iddic.put(idtrans, String.valueOf(i));
			includenodes.add(idtrans);
		}

		System.out.println("INCLUDE:"+includenodes.size());
		String linkcontent = FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/0423_cite_pair.txt");
		String[] linkrows = linkcontent.split("\n");
		JSONArray links = new JSONArray();
		for (String lr : linkrows) {
			String[] enlr = lr.split("\t");
			if (includenodes.contains(enlr[0]) && includenodes.contains(enlr[1])) {
				int sint = Integer.valueOf(iddic.get(enlr[0]));
				int tint = Integer.valueOf(iddic.get(enlr[1]));
				JSONObject jo = new JSONObject();
				jo.accumulate("source", sint);
				jo.accumulate("target", tint);
				jo.accumulate("value", 1);
				links.add(jo);
			}
		}
		JSONObject data = new JSONObject();
		data.accumulate("nodes", nodes);
		data.accumulate("links", links);
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/0423subdata.json", data.toString());

	}

}
