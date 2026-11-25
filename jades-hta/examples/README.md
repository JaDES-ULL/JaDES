# Validation examples of OSDi

Each example represents a published study in OSDi and checks that JaDES reproduces its results. Unlike the tests, which check one feature at a time with values computed by hand, an example checks a whole model against what its authors published.

Each folder has:

- the ontology of the example, named as its folder (`<case>/<case>.ttl`), which imports OSDi. Its IRI is `https://w3id.org/ontologies-ULL/OSDi/examples/<case>` (the latest version) and its version IRI, `https://w3id.org/ontologies-ULL/OSDi/<X.Y>/examples/<case>`. With a text/html request, these IRIs lead to the folder of the example in GitHub; otherwise, to its Turtle file;
- `catalog-v001.xml`, which resolves the import of OSDi with the copy at the root of the repository (e.g., in Protégé);
- a `README.md` with the citation, the decisions of representation, the assumptions of the article made explicit and the known differences with the published results;
- `expected-results.json`, with the expected values. Each value states its source: `published` (with its tolerance and where it appears in the article) or `hand` (computed by hand from the model, with a relative tolerance of 1e-8).

Run the validation with `mvn -pl jades-hta -am test -Pvalidation` from the root of JaDES. It writes a report with the expected value, the value of JaDES and the difference of each example to `jades-hta/target/validation/report.md`. To generate the Excel of an example outside the tests:

```bash
java -cp <classpath> es.ull.simulation.hta.osdi.OSDiMain genExcel --osdiPath examples/evans1997-migraine/evans1997-migraine.ttl --osdiExperiment EVANS_Experiment --excelPath evans.xlsm
```

| Example | Reference | Health problem | Validated model types | Parts of OSDi it exercises | Status |
| --- | --- | --- | --- | --- | --- |
| [evans1997-migraine](evans1997-migraine/README.md) | Evans KW et al. *PharmacoEconomics*. 1997;12(5):565-577 | Migraine attack: oral sumatriptan versus caffeine/ergotamine | Decision tree | Acute manifestation as the target (D1), strategy replacements (D2), strategy outcomes and resolution (D4), group of alternative strategies with proportions, relapse after the response (D9), one-time QALY losses, including a negative one (D7), time horizon of 24 hours, costs in Canadian dollars of the year of the study | Reproduces the published ICER (29 363 vs. 29 366 CAD/QALY) |
