# Quick run script for University ERP
# Sets JAVA_HOME and runs the application

$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
.\mvnw.cmd clean compile exec:java "-Dexec.mainClass=edu.univ.erp.ui.Main"

