package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import eppic.MolViewersHelper;
import eppic.model.ResidueBurialDB;

public class JmolPageGenerator 
{
    /**
     * Generates html page containing jmol aplet.
     * @param title title of the page
     * @param size 
     * @param serverUrl url to the server where jmol and results are stored
     * @param resultsLocation path to results on the server
     * @param fileName name of the cif file
     * @param interfData 
     * @param url3dmoljs
     * @return html page with jmol aplet
     */
	public static String generatePage(String title, String size, String serverUrl, String resultsLocation,
			String fileName, Interface interfData, Assembly assemblyData, String url3dmoljs)  {
		
		
		boolean isCif = true;
		String dataTypeString = ""; // if neither of the 2 cases then we leave it blank so that 3dmol guesses
		if (fileName.endsWith("cif")) {
			dataTypeString = "data-type='cif' ";
			isCif = true;
		} else if (fileName.endsWith("pdb")) {
			dataTypeString = "data-type='pdb' ";
			isCif = false;
		}
		
//		String selectionCode;
//		if (interfData != null) {
//			selectionCode = generateInterfaceSelection3dmolCode(interfData, isCif);
//		} else if (assemblyData !=null ){
//			selectionCode = generateAssemblySelection3dmolCode(assemblyData, isCif);
//		} else {
//			// a default that at least shows a cartoon
//			return "data-style='cartoon'\n";
//		}
		
		
		StringBuffer jmolPage = new StringBuffer();

		String fileUrl = serverUrl + "/" + resultsLocation + "/" + fileName;

		jmolPage.append("<!DOCTYPE html>");
		jmolPage.append("<html>" + "\n");
		jmolPage.append("<head>" + "\n");
		jmolPage.append("<title>" + "\n");
		jmolPage.append(title+"\n"); 
		jmolPage.append("</title>" + "\n");

		jmolPage.append("<script src=\""+url3dmoljs+"\"></script> \n");

		jmolPage.append("</head>" + "\n");
		jmolPage.append("<body>" + "\n");

		jmolPage.append(
		
		"<script>"+

		"if( !Detector.webgl ) Detector.addGetWebGLMessage();"+

		"NGL.mainScriptFilePath = \""+url3dmoljs+"\";"+

		"function onInit(){"+
		"	var stage = new NGL.Stage( \"viewport\" );"+
		"	stage.loadFile( \""+fileUrl+"\", { defaultRepresentation: true } );"+
		"}"+

		"document.addEventListener( \"DOMContentLoaded\", function() {"+
		"	NGL.init( onInit );"+
		"} );"+

		"</script>"+

		"<div id=\"viewport\" style=\"width:"+size+"px; height:"+size+"px;\"></div>");
		
//		jmolPage.append(
//				"<div style=\"height: "+size+"px; width: "+size+"px; position: relative;\" "+
//				"class='viewer_3Dmoljs' \n" +
//				"data-href='"+fileUrl+"' data-backgroundcolor='0xffffff' "+
//				dataTypeString +
//				"\n"+
//				
//				selectionCode +
//				
//				">\n"+
//				
//				"</div>\n");
		
		jmolPage.append("</body>" + "\n");
		jmolPage.append("</html>" + "\n");

		return jmolPage.toString();
    }
	
	private static String getCommaSeparatedList(List<Residue> residues) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<residues.size();i++){
			sb.append(residues.get(i).getPdbResidueNumber());
			if (i!=residues.size()-1) sb.append(',');
		}
		return sb.toString();
	}
	
	private static String generateInterfaceSelection3dmolCode(Interface interfData, boolean isCif) {
		String chain1 = interfData.getChain1();		
		String chain2 = interfData.getChain2(); 
		///boolean isSymRelated = false;
		
		if (chain1.equals(chain2)) {
			//isSymRelated = true;
			if (isCif) {
				// exactly as done in StructureInterface.toMMCIF()
				chain2 = chain2 +"_"+ interfData.getOperatorId();
			} else {
				// exactly as done in StructureInterface.toPDB()
				// NOTE this won't work with chain ids of more than 1 char
				chain2 = Character.toString(MolViewersHelper.getNextLetter(chain1.charAt(0)));
			}
		}
		
		String color1 = MolViewersHelper.getHexChainColor(chain1);
		String color2 = MolViewersHelper.getHexChainColor(chain2);
		
		String colorCore1 = MolViewersHelper.getHexInterf1Color();
		String colorCore2 = MolViewersHelper.getHexInterf2Color();
				
		List<Residue> coreResidues1 = new ArrayList<Residue>();
		List<Residue> rimResidues1 = new ArrayList<Residue>();
		List<Residue> coreResidues2 = new ArrayList<Residue>();
		List<Residue> rimResidues2 = new ArrayList<Residue>();
		
		for (Residue residue:interfData.getResidues() ) {
			
			if (residue.getRegion()==ResidueBurialDB.CORE_EVOLUTIONARY || residue.getRegion()==ResidueBurialDB.CORE_GEOMETRY) {
				if (residue.getSide()==false) {
					coreResidues1.add(residue);
				} else if (residue.getSide()==true) {
					coreResidues2.add(residue);
				}
			} else if (residue.getRegion()==ResidueBurialDB.RIM_EVOLUTIONARY) {
				if (residue.getSide()==false) {
					rimResidues1.add(residue);
				} else if (residue.getSide()==true) {
					rimResidues2.add(residue);
				}
			}
		}

		return 
				// chain 1
				"data-select1='chain:"+chain1+"' "+
				"data-style1='cartoon:color="+color1+"' "+
				"\n"+

				// chain 2
				"data-select2='chain:"+chain2+"' "+
				"data-style2='cartoon:color="+color2+"' "+
				"\n"+
		
				// core residues 1
				"data-select3='resi:"+getCommaSeparatedList(coreResidues1)+";chain:"+chain1+"' "+
				"data-style3='cartoon:color="+color1+";stick:color="+colorCore1+"' "+
				"\n"+
		
				// rim residues 1
				"data-select4='resi:"+getCommaSeparatedList(rimResidues1)+";chain:"+chain1+"' "+
				"data-style4='cartoon:color="+color1+";stick:color="+color1+"' "+
				"\n"+
		
				// core residues 2
				"data-select5='resi:"+getCommaSeparatedList(coreResidues2)+";chain:"+chain2+"' "+
				"data-style5='cartoon:color="+color2+";stick:color="+colorCore2+"' "+
				"\n"+
		
				//rim residues 2
				"data-select6='resi:"+getCommaSeparatedList(rimResidues2)+";chain:"+chain2+"' "+
				"data-style6='cartoon:color="+color2+";stick:color="+color2+"' "+
				"\n";

	}
	
	private static String generateAssemblySelection3dmolCode(Assembly assemblyData, boolean isCif) {
		String chainIdsString = assemblyData.getChainIdsString();
		
		
		
		if (chainIdsString!=null) {
			StringBuilder sb = new StringBuilder();
			String[] chainIds = chainIdsString.split(",");
			
			int i = 1;
			for (String chainId:chainIds) {
				sb.append(
				"data-select"+i+"='chain:"+chainId+"' "+
				"data-style"+i+"='cartoon:color="+MolViewersHelper.getHexChainColor(chainId)+"' "+
				"\n");
				i++;
			}
			
			return sb.toString();
		}
		
		// no chainIds, simply cartoon with same color for all
		return "data-style='cartoon'\n";
	}
}
