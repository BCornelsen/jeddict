/**
 * Copyright [2014] Gaurav Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.netbeans.jpa.modeler.source.generator.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.jcode.console.Console;
import static org.netbeans.jcode.console.Console.FG_MAGENTA;
import org.netbeans.jcode.generator.JEEApplicationGenerator;
import org.netbeans.jcode.stack.config.data.ApplicationConfigData;
import org.netbeans.jcode.task.AbstractNBTask;
import org.netbeans.jcode.task.progress.ProgressConsoleHandler;
import org.netbeans.jcode.task.progress.ProgressHandler;
import org.netbeans.jeddict.analytics.JeddictLogger;
import org.netbeans.jpa.modeler.source.generator.adaptor.ISourceCodeGenerator;
import org.netbeans.jpa.modeler.source.generator.adaptor.ISourceCodeGeneratorFactory;
import org.netbeans.jpa.modeler.source.generator.adaptor.SourceCodeGeneratorType;
import org.netbeans.jpa.modeler.source.generator.adaptor.definition.InputDefinition;
import org.netbeans.jpa.modeler.source.generator.adaptor.definition.orm.ORMInputDefiniton;
import org.netbeans.jpa.modeler.spec.EntityMappings;
import org.netbeans.jpa.modeler.specification.model.util.PreExecutionUtil;
import org.netbeans.modeler.core.ModelerFile;
import org.netbeans.orm.converter.generator.PersistenceXMLGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class SourceCodeGeneratorTask extends AbstractNBTask {

    private final ModelerFile modelerFile;
    private final ApplicationConfigData appConfigData;
    private final Runnable afterExecution;

    private final static int SUBTASK_TOT = 1;
    private static String BANNER_TXT;

    public SourceCodeGeneratorTask(ModelerFile modelerFile, ApplicationConfigData appConfigData, Runnable afterExecution) {
        this.modelerFile = modelerFile;
        this.appConfigData = appConfigData;
        this.afterExecution=afterExecution;
        if (BANNER_TXT == null) {
            try (InputStream stream = getClass().getResourceAsStream("banner")) {
                BANNER_TXT = Console.wrap(IOUtils.toString(stream), FG_MAGENTA);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        initialize();
    }

    @Override
    protected void initTask() {
        setLogLevel(TERSE);
        setTaskName(getBundleMessage("LBL_GenerateCodeDialogTitle") + " - " + appConfigData.getTargetArtifactId()); 
        setDisplayOutput(true);

        progressContribs = new ProgressContributor[SUBTASK_TOT];
        int i = 0;
        progressContribs[i] = AggregateProgressFactory
                .createProgressContributor(getBundleMessage("MSG_Processing")); // NOI18N
    }

    @Override
    protected void begin() {
        // Issue Fix #5847 Start
        if (!modelerFile.getModelerPanelTopComponent().isPersistenceState()) {
            this.log("Saving " + modelerFile.getName() + " File..\n");
            modelerFile.getModelerUtil().saveModelerFile(modelerFile);//synchronous
            modelerFile.getModelerScene().getModelerPanelTopComponent().changePersistenceState(true);//remove * from header
        } else {
            PreExecutionUtil.preExecution(modelerFile);
        }
        // Issue Fix #5847 End
        try {
            exportCode();
        } catch (Throwable t) {
            modelerFile.handleException(t);
        }
    }

    @Override
    protected void finish() {
        if (afterExecution != null) {
            RequestProcessor.getDefault().post(afterExecution);
        }
    }

    /**
     * @param elements The collection of elements to generate for
     *
     */
    private void exportCode() {
        ProgressHandler handler = new ProgressConsoleHandler(this);
        handler.append(BANNER_TXT);
        
        ISourceCodeGeneratorFactory sourceGeneratorFactory = Lookup.getDefault().lookup(ISourceCodeGeneratorFactory.class);
        ISourceCodeGenerator sourceGenerator = sourceGeneratorFactory.getSourceGenerator(SourceCodeGeneratorType.JPA);
        InputDefinition definiton = new ORMInputDefiniton();
        definiton.setModelerFile(modelerFile);
        EntityMappings entityMappings = (EntityMappings) modelerFile.getDefinitionElement();
        JEEApplicationGenerator applicationGenerator = null;
        
        entityMappings.cleanRuntimeArtifact();
        appConfigData.setEntityMappings(entityMappings);
        if (appConfigData.getBussinesTechContext()!= null) {
            applicationGenerator = new JEEApplicationGenerator(appConfigData, handler);
            applicationGenerator.preGeneration();
        }
        
        if (appConfigData.isMonolith() || appConfigData.isMicroservice()) {
            sourceGenerator.generate(this,
                    appConfigData.getTargetProject(),
                    appConfigData.getTargetSourceGroup(),
                    definiton);
        }
        if (appConfigData.isGateway()) {
            PersistenceXMLGenerator persistenceXMLGenerator = new PersistenceXMLGenerator(Collections.emptyList());
            persistenceXMLGenerator.setPUName(appConfigData.getEntityMappings().getPersistenceUnitName());
            persistenceXMLGenerator.generatePersistenceXML(appConfigData.getGatewayProject(), appConfigData.getGatewaySourceGroup());
        }
        
        if (appConfigData.getBussinesTechContext()!= null) {
            applicationGenerator.generate();
            applicationGenerator.postGeneration();
        }
        JeddictLogger.logGenerateEvent(appConfigData);
    }

    private static String getBundleMessage(String key) {
        return NbBundle.getMessage(SourceCodeGeneratorTask.class, key);
    }

}
