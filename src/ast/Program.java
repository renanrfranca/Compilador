/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

import java.util.*;
import comp.CompilationError;

public class Program {

	public Program(ArrayList<CianetoClass> classList, ArrayList<MetaobjectAnnotation> metaobjectCallList, 
			       ArrayList<CompilationError> compilationErrorList) {
		this.classList = classList;
		this.metaobjectCallList = metaobjectCallList;
		this.compilationErrorList = compilationErrorList;
	}

	public ArrayList<CianetoClass> getClassList() {
		return classList;
	}


	public ArrayList<MetaobjectAnnotation> getMetaobjectCallList() {
		return metaobjectCallList;
	}
	

	public boolean hasCompilationErrors() {
		return compilationErrorList != null && compilationErrorList.size() > 0 ;
	}

	public ArrayList<CompilationError> getCompilationErrorList() {
		return compilationErrorList;
	}

	public void addClass(CianetoClass cClass){
		this.classList.add(cClass);
	}
	
	private ArrayList<CianetoClass> classList;
	private ArrayList<MetaobjectAnnotation> metaobjectCallList;
	private ArrayList<CompilationError> compilationErrorList;
	
}