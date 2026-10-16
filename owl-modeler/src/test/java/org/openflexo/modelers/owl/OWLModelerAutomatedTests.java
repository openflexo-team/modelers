package org.openflexo.modelers.owl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.owl.OWLTechnologyAdapter;

/**
 * A parameterized suite of FML-script driven integration tests for the OWL modeler.
 *
 * <p>
 * Each {@code .fmlscript} under {@code src/main/resources/AutomatedTests} is parsed and executed; every {@code assert} it contains must
 * succeed.
 *
 * <p>
 * Unlike the other modelers of this repository, these scripts assert STRUCTURE only. The editor's creation scheme requires a
 * {@code Resource<OWLOntology>} and this resource center ships no ontology, so there is nothing here to run it on. Giving this modeler a
 * real execution suite means adding {@code owl-ta-test-rc} as a test-scope resource center - it ships BasicOntologyEditor.owl, animals.owl
 * and PizzaOntology.owl - which is a scope decision, not a migration one.
 *
 * @see TestOWLModeler
 */
@RunWith(Parameterized.class)
public class OWLModelerAutomatedTests extends FMLScriptParserTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("AutomatedTests"), ".fmlscript");
	}

	private final Resource fmlResource;
	private FlexoEditor editor;
	private FMLScript script;
	private CommandInterpreter commandInterpreter;

	public OWLModelerAutomatedTests(Resource fmlResource, String name) throws ParseException, ModelDefinitionException, IOException {
		System.out.println("********* Launch FML-script " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
		initServiceManager();
	}

	@Test
	public void checkScript() throws ModelDefinitionException, ParseException, IOException, FMLCommandExecutionException {
		System.out.println("Parse script " + fmlResource.getRelativePath());
		script = parseFMLScript(fmlResource, commandInterpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}

	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager(OWLTechnologyAdapter.class, DiagramTechnologyAdapter.class);

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
	}

}
