package org.openflexo.modelers.owl;

/**
 * The URIs published by the OWL modeler resource center.
 *
 * <p>
 * These used to live in the <code>http://openflexo.org/modellers</code> resource center shared by the four modelers. The OWL modeler now
 * carries its own resource center, hence its own URI space.
 */
public class OWLModelerConstants {

	/** Base URI of the OWL modeler resource center, as declared by META-INF/resourceCenters. */
	public static final String OWL_MODELER_RC_URI = "http://openflexo.org/owl-modeler";

	/** The editor: OWL classes and individuals, with their SubClass, Type and ObjectProperty relationships. */
	public static final String OWL_ONTOLOGY_EDITOR_URI = OWL_MODELER_RC_URI + "/FML/OWLOntologyEditor.fml";

	/** The diagram specification the editor is typed by. */
	public static final String OWL_ONTOLOGY_EDITOR_SPECIFICATION_URI = OWL_MODELER_RC_URI
			+ "/DiagramSpecs/OWLOntologyEditorSpec.diagramspecification";

}
