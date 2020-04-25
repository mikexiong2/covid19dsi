package edu.columbia.dbmi.covid.covidpub;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
    		String dicstr=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/pub_meta_672.txt");
    		String[] dicrows=dicstr.split("\n");
    		Map<String,String> map=new HashMap<String,String>();
    		Map<String,String> pid2pmc=new HashMap<String,String>();
    		Map<String,Integer> pmc2id=new HashMap<String,Integer>();
    		List<String> pmcidlist=new ArrayList<String>();
    		JSONArray nodes=new JSONArray();
    		for(int i=0;i<dicrows.length;i++){
    			String dicr=dicrows[i];
    			System.out.println(dicr);
    			String[] dicen=dicr.split("\t");
    			if(dicen[1].equals("0")==false){
    				map.put(dicen[1], dicen[2]);
    				map.put(dicen[1], dicen[0]);
    			}
    			pmcidlist.add(dicen[0]);
    			pmc2id.put(dicen[0], i);
			JSONObject node=new JSONObject();
			node.accumulate("name", dicen[0]);
			node.accumulate("group", 1);
			nodes.add(node);
    		}
    		String pmcrefpidstr=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/pm_refs_all.txt");
		String[] prprows=pmcrefpidstr.split("\n");
		StringBuffer onlycovid19sb=new StringBuffer();
		JSONArray links=new JSONArray();
		for(String ppr:prprows){
			//System.out.println(ppr);
			String[] pen=ppr.split("\t");
			if(map.containsKey(pen[1])){
				//System.out.println(pen[0]+"-------->"+pen[1]);
				String sourcePMCID="PMC"+pen[0];
				String targetPMCID=map.get(pen[1]);
				System.out.println(sourcePMCID+"-------->"+targetPMCID);
				System.out.println(pmc2id.get(sourcePMCID)+"-------->"+pmc2id.get(targetPMCID));
				//onlycovid19sb.append(sourcePMCID+"\t"+targetPMCID+"\n");
				JSONObject jo=new JSONObject();
				jo.accumulate("source", pmc2id.get(sourcePMCID));
				jo.accumulate("target", pmc2id.get(targetPMCID));
				jo.accumulate("value", 1);
				links.add(jo);
			}
		}
		JSONObject data=new JSONObject();
		data.accumulate("nodes", nodes);
		data.accumulate("links", links);
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/data2.json", data.toString());	
		
		//FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/pmc_ref_pmc.txt", onlycovid19sb.toString());	
		
    		/*
    		String pmcrefpidstr=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/pm_refs_all.txt");
		String[] pprows=pmcrefpidstr.split("\n");
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		for(String ppr:pprows){
			System.out.println(ppr);
			String pmcId=ppr.split("\t")[0];
			String refpid=ppr.split("\t")[1];
			if(map.containsKey(refpid)){
				Integer count=map.get(refpid);
				count++;
				map.put(refpid, count);
			}else{
				map.put(refpid, 1);
			}
		}
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, Integer> mapping : list) {
			//System.out.println(mapping.getKey() + ":" + mapping.getValue());
			sb.append(mapping.getKey() + "\t" + mapping.getValue() + "\n");
		}
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/refs_count.txt", sb.toString());	
    		/*
    		//query metam data
    		String pmcrefpidstr=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/pm_refs_all.txt");
		String[] pprows=pmcrefpidstr.split("\n");
		Set<String> pmcIdset=new HashSet<String>();
		for(String ppr:pprows){
			System.out.println(ppr);
			String pmcId=ppr.split("\t")[0];
			pmcIdset.add(pmcId);
		}
		
		System.out.println(pmcIdset.size());
		StringBuffer sb=new StringBuffer();
		int count=0;
		for(String pmcId:pmcIdset){
			Publication pub=query4MetaInfoByPmcId(pmcId);
			sb.append(pub.getPmcid()+"\t"+pub.getPmid()+"\t"+pub.getDoi()+"\n");
			count++;
			System.out.println(count +" of "+pmcIdset.size());
			if(count%50==0 || count == (pmcIdset.size()-1)){
				FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/pub_meta_"+count+".txt", sb.toString());
			}
		}
		*/
		
		/*
    		String pmcidstr=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/pmc_result_pmcIDs.txt");
    		String[] pmcrows=pmcidstr.split("\n");
    		StringBuffer sb=new StringBuffer();
    		int count=0;
    		for(String pr:pmcrows){
    			System.out.println("=>"+count+" of "+pmcrows.length);
    			System.out.println("=>"+pr.substring(3));
    			String pmcid=pr.substring(3);
    			String xmlcontent=HttpUtil.doGet("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id="+pmcid);
    			List<String> pmids=parseXML4refpmIds(xmlcontent);
    			for(String pmid:pmids){
    				sb.append(pmcid+"\t"+pmid+"\n");
    			}
    			count++;
    			//if( count%50==0 || count == pmcrows.length){
    				//}
    		}
    		//FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/pm_refs_left_"+count+".txt", sb.toString());
    		 */
    		/*
        String content=HttpUtil.doGet("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id=4304705");
        System.out.println(content);
        List<String> pmids=parseXML(content);
        */
    }
    
    public static Article query4MetaInfoByPmcId(String pmcId){
    		Article pub=new Article();
    		String toolname="mytool";
    		String email="my_email@example.com";
    		String jsonContent=HttpUtil.doGet("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pmc&id="+pmcId+"&retmode=json&tool="+toolname+"&email="+email);
    		JSONObject jo=JSONObject.fromObject(jsonContent);
    		JSONObject rejo=(JSONObject) jo.get("result");
    		JSONObject punit=(JSONObject) rejo.get(pmcId);
    		String title=(String) punit.get("title");
    		System.out.println(title);
    		pub.setTitle(title);
    		JSONArray ja=(JSONArray) punit.get("articleids");
    		for(int i=0;i<ja.size();i++){
    			JSONObject articleId=(JSONObject) ja.get(i);
    			String idtype=(String) articleId.get("idtype");
    			String id=(String) articleId.get("value");
    			System.out.println(idtype+"\t"+id);
    			if(idtype.equals("pmid")){
    				pub.setPmid(id);
    			}else if(idtype.equals("doi")){
    				pub.setDoi(id);
    			}else if(idtype.equals("pmcid")){
    				pub.setPmcid(id);
    			}
    		}
    		String pubdate=(String) punit.get("pubdate");
    		pub.setPubdate(pubdate);
    		String source=(String) punit.get("source");
    		pub.setSource(source);
    		String volume=(String) punit.get("volume");
    		pub.setVolume(volume);
    		String issue=(String) punit.get("issue");
    		pub.setIssue(issue);
    		String pages=(String) punit.get("pages");
    		pub.setPages(pages);
    		return pub;
    }
    
    
    public static List<String> parseXML4refpmIds(String protocolXML) {
		try {
			List<String> refs=new ArrayList<String>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(protocolXML)));
			NodeList list = doc.getElementsByTagName("ref");
			if(list.getLength()>1){
				//System.out.println(">>>>>>>>1");
			}
			for(int i=0;i<list.getLength();i++){
				Element element = (Element) list.item(i);
				//element.getChildNodes();
				NodeList reflist =element.getElementsByTagName("pub-id");
				//System.out.println("_____");
				for(int j=0;j<reflist.getLength();j++){
					Node n=reflist.item(j);
					NamedNodeMap nnmp=n.getAttributes();
					String pub_id_type=nnmp.getNamedItem("pub-id-type").getNodeValue();
					//System.out.println(pub_id_type);
					//System.out.println(reflist.item(j).getTextContent());
					String refpmid=reflist.item(j).getTextContent();
					if(pub_id_type.equals("pmid")){
						refs.add(refpmid);
					}
				}
			}
			return refs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
