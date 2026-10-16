modelers
========

Openflexo specific modeling environments &amp; tools built upon Openflexo software infrastructure

Projects
--------

* **bpmn-modeler** — the BPMN 2.0 modeler, a self-contained project: it carries its API
  (`src/main/java`), its resource center (`src/main/resources`, base URI
  `http://openflexo.org/bpmn-modeler`) and its tests (`src/test/java`, plus the FML-scripts of
  `src/main/resources/AutomatedTests`). It federates an EMF `.bpmn` model and projects each of its
  processes on a diagram.

* **uml-modeler** — the UML class modeler, same shape: its API, its resource center (base URI
  `http://openflexo.org/uml-modeler`) and its tests. Classes and properties in pure FML (there is
  no UML technology adapter in 2.99), with a class-diagram view on top.

* **statecharts-modeler** — states and the transitions between them, in pure FML, with a diagram
  view. Base URI `http://openflexo.org/statecharts-modeler`.

* **owl-modeler** — a basic OWL ontology editor: classes and individuals with their SubClass, Type
  and ObjectProperty relationships, projected on a diagram. Base URI
  `http://openflexo.org/owl-modeler`.

Every modeler follows the same shape, the one of `openflexo-integration-tests/city-mapping`: API +
resource center + tests in a single project. There is no shared `modelers-rc` any more — the four
modelers used to live in one, under `http://openflexo.org/modellers`, and were split out one by
one. Their resource centers may NOT share a base URI: `DefaultResourceCenterService` skips any
resource center whose `defaultBaseURI` is already registered, so two of them sharing one would
silently mask each other.

Building & testing
------------------

These modelers validate against FML attributes carried by the GINA and diagram technology
adapters, so run the tests through those included builds (or against published snapshots that
carry them):

    ./gradlew --include-build ../openflexo-gina --include-build ../openflexo-diagram test
