package io.realm;

import com.intellij.codeInspection.InspectionToolProvider;

public class RealmInspectionsProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[] { MissingRealmClassAnnotationInspection.class};
  }
}
