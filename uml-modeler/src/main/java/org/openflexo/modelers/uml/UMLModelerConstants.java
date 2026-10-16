package org.openflexo.modelers.uml;

/**
 * The URIs published by the UML modeler resource center.
 *
 * <p>
 * The UML modeler used to live in the <code>http://openflexo.org/modellers</code> resource center shared by the four modelers, where it
 * still carried its 2012 URIs - the editor was addressed as <code>.../UMLEditor/UMLEditor.viewpoint</code>, a suffix that means nothing in
 * 2.99. It now carries its own resource center, hence its own URI space.
 */
public class UMLModelerConstants {

	/** Base URI of the UML modeler resource center, as declared by META-INF/resourceCenters. */
	public static final String UML_MODELER_RC_URI = "http://openflexo.org/uml-modeler";

	/** The editor itself: holds the root UMLModel instance. */
	public static final String UML_EDITOR_URI = UML_MODELER_RC_URI + "/FML/UMLEditor.fml";

	/** The abstract syntax: classes and their properties, in pure FML. */
	public static final String UML_MODEL_URI = UML_EDITOR_URI + "/UMLModel.fml";

	/** The class-diagram view of a UMLModel. */
	public static final String UML_CLASS_DIAGRAM_URI = UML_EDITOR_URI + "/UMLClassDiagram.fml";

	/** The diagram specification the UMLClassDiagram VirtualModel is typed by. */
	public static final String UML_CLASS_DIAGRAM_SPECIFICATION_URI = UML_MODELER_RC_URI
			+ "/DiagramSpecs/UMLClassDiagramSpecification.diagramspecification";

}
