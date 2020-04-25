package edu.columbia.dbmi.covid.covidpub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONDataFetcher {

	public static void main(String[] args) {
		StringBuffer paper_ref = new StringBuffer();
		StringBuffer ref_pos=new StringBuffer();
		File dir = new File("/Users/cy2465/Downloads/pubmed_ref_project/custom_license/pmc_json");
		System.out.println(dir.listFiles().length);
		int fcount = 0;
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".json") == false) {
				continue;
			}
			System.out.println(fcount++);
			String content = FileUtil.readFile(f.getAbsolutePath());
			JSONObject jo = JSONObject.fromObject(content);
			/**
			 * Meta data
			 */
			// System.out.println(jo.get("paper_id"));
			String paper_id = (String) jo.get("paper_id");
			// System.out.println(jo.get("metadata"));
			JSONObject metadatajson = (JSONObject) jo.get("metadata");
			JSONArray ja = (JSONArray) metadatajson.get("authors");
			String paper_title = "Unknown";
			paper_title = (String) metadatajson.get("title");
			
			JSONArray pauthorja = (JSONArray) metadatajson.get("authors");
			String pfirst_au_firstname = "Unknown";
			String pfirst_au_lastname = "Unknown";
			String plast_au_firstname = "Unknown";
			String plast_au_lastname = "Unknown";
			if (pauthorja.size() > 0) {
				JSONObject pfirstauthor = (JSONObject) pauthorja.get(0);
				JSONObject plastauthor = (JSONObject) pauthorja.get(pauthorja.size() - 1);
				pfirst_au_firstname = pfirstauthor.get("first").toString();
				pfirst_au_lastname = pfirstauthor.get("last").toString();
				plast_au_firstname = plastauthor.get("first").toString();
				plast_au_lastname = plastauthor.get("last").toString();
			}
			/**
			 * Text
			 * 
			 */
			JSONArray body_textja = jo.getJSONArray("body_text");
			// System.out.println(jo.get("body_text"));
			List<String> biblist = new ArrayList<String>();
			int count = 0;
			// System.out.println("JA=" + body_textja.size());
			for (int j = 0; j < body_textja.size(); j++) {
				// System.out.println("bt id=" + j);
				JSONObject textjo = (JSONObject) body_textja.get(j);
				// System.out.println(textjo.get("text"));
				// System.out.println(textjo.get("cite_spans"));
				String text = textjo.get("text").toString();
				JSONArray cite_spans_ja = (JSONArray) textjo.get("cite_spans");
				// System.out.println(cite_spans_ja.size());
				int bpos = 0;
				count = count + cite_spans_ja.size();
				for (int k = 0; k < cite_spans_ja.size(); k++) {
					JSONObject citejo = (JSONObject) cite_spans_ja.get(k);
					Integer start = Integer.valueOf(citejo.get("start").toString());
					Integer end = Integer.valueOf(citejo.get("end").toString());
					String mention = citejo.get("mention").toString();
					// System.out.println("subtext=" + subtext);
					// System.out.println("citation=" + text.substring(start,
					// end));
					// System.out.println(citejo.get("start") + "-" +
					// citejo.get("end"));
					// System.out.println("Section:" +
					// textjo.get("section").toString().replace("\n", " "));
					// System.out.println(citejo.get("ref_id").toString());
					// System.out.println("----");
					// ref_id
					ref_pos.append(paper_id+"\t"+text+"\t"+start+"\t"+end+"\t"+mention+"\t"+citejo.get("ref_id").toString()+"\n");
					biblist.add(citejo.get("ref_id").toString());
				}
			}
			/**
			 * bib
			 */
			// System.out.println("bib size=" + biblist.size());
			// System.out.println("bib size=" + count);
			JSONObject bib_entries = (JSONObject) jo.get("bib_entries");

			if (jo.containsKey("bib_entries")) {
				for (String bibstr : biblist) {
					// System.out.println("->" + bibstr);
					// System.out.println("->" + bib_entries.get(bibstr));
					JSONObject bibinfo = (JSONObject) bib_entries.get(bibstr);
					//System.out.println(bibinfo);
					String title = "Unknown";
					if (bibinfo != null) {
						title = (String) bibinfo.get("title");
					}
					//System.out.println(paper_id + "\t" + title);
					String first_au_firstname = "Unknown";
					String first_au_lastname = "Unknown";
					String last_au_firstname = "Unknown";
					String last_au_lastname = "Unknown";
					if (bibinfo != null) {
						JSONArray authorja = (JSONArray) bibinfo.get("authors");

						if (authorja.size() > 0) {
							JSONObject firstauthor = (JSONObject) authorja.get(0);
							JSONObject lastauthor = (JSONObject) authorja.get(authorja.size() - 1);
							first_au_firstname = firstauthor.get("first").toString();
							first_au_lastname = firstauthor.get("last").toString();
							last_au_firstname = lastauthor.get("first").toString();
							last_au_lastname = lastauthor.get("last").toString();
						}
					}
					paper_ref.append(paper_id + "\t" + paper_title + "\t" + pfirst_au_firstname + "\t"
							+ pfirst_au_lastname + "\t" + plast_au_firstname + "\t" + plast_au_lastname + "\t" + title
							+ "\t" + first_au_firstname + "\t" + first_au_lastname + "\t" + last_au_firstname + "\t"
							+ last_au_lastname + "\n");
				}
			}
		}
		
		//FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/custom_license/pmc_json_combined.txt",
		//		paper_ref.toString());

		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/custom_license/pmc_json_ref_plus_context.txt",
						ref_pos.toString());
	}

}
