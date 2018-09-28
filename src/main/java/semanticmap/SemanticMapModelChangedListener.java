package semanticmap;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class SemanticMapModelChangedListener implements ModelChangedListener {

	public SemanticMapModelChangedListener() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addedStatement(Statement s) {
		
	}

	@Override
	public void addedStatements(Statement[] statements) {

	}

	@Override
	public void addedStatements(List<Statement> statements) {

	}

	@Override
	public void addedStatements(StmtIterator statements) {

	}

	@Override
	public void addedStatements(Model m) {

	}

	@Override
	public void removedStatement(Statement s) {

	}

	@Override
	public void removedStatements(Statement[] statements) {

	}

	@Override
	public void removedStatements(List<Statement> statements) {

	}

	@Override
	public void removedStatements(StmtIterator statements) {

	}

	@Override
	public void removedStatements(Model m) {

	}

	@Override
	public void notifyEvent(Model m, Object event) {

	}

}
