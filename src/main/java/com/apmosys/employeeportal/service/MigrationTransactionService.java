package com.apmosys.employeeportal.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
// import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.TimesheetDocumentMetaDto;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.MigratedDoc;
import com.apmosys.employeeportal.model.TempFailedDoc;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.MigratedDocRepository;
import com.apmosys.employeeportal.repository.TempFailedDocRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;

@Service
public class MigrationTransactionService {
    
     @Autowired
    private TimesheetDocumentDetailsNewRepository timesheetDocumentDetailsNewRepository;

    @Autowired
    private FinalDocumentNewRepository finalDocumentNewRepository;

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private TimesheetDocumentServiceNew timesheetDocumentServiceNew;

    @Autowired
    private TempFailedDocRepository tempFailedDocRepository;
   
    @Autowired
    private MigratedDocRepository migratedDocRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void migrateSingleEntry(
            TimesheetDocumentMetaDto doc,
            Integer projectId,
            byte[] docData,
            Path storageDir) throws Exception {
        
               int val = 0;
               if(doc.getFinalFlag()){
                val =1;
               }
               else{
                val = 0;
               }
        String uniqueFileName = projectId + "_" + val + "_" + doc.getDocName();
        Files.write(storageDir.resolve(uniqueFileName), docData);
        timesheetDocumentDetailsNewRepository.save(
        buildTimesheetDocumentDetailsNewFromDto(doc, projectId, uniqueFileName, null, false));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void migrateMultiEntry(
            TimesheetDocumentMetaDto finalDoc,
            TimesheetDocumentMetaDto pendingDoc,
            Integer projectId,
            byte[] finalDocData,
            byte[] pendingDocData,
            Path storageDir) throws Exception {

        int val =0;
        if(finalDoc.getFinalFlag()){
            val = 1;
        }else{
            val = 0;
        }
        String finalFileName = projectId + "_" + val + "_" + finalDoc.getDocName();
        Files.write(storageDir.resolve(finalFileName), finalDocData);
        
        FinalDocumentNew savedFinalDoc = finalDocumentNewRepository.save(
        buildFinalDocumentNewFromDto(finalDoc, projectId, finalFileName));

        entityManager.flush();

        if (pendingDoc != null && pendingDocData != null) {
            String pendingFileName = projectId + "_" + val + "_" + pendingDoc.getDocName();
            Files.write(storageDir.resolve(pendingFileName), pendingDocData);

            timesheetDocumentDetailsNewRepository.save(
            buildTimesheetDocumentDetailsNewFromDto(
            pendingDoc, projectId, pendingFileName, savedFinalDoc.getFinalDocId(), true));
        }
    }




        public TimesheetDocumentDetailsNew buildTimesheetDocumentDetailsNewFromDto(
        TimesheetDocumentMetaDto doc,
        Integer projectId,
        String uniqueFileName,
        Long bulkApprovedDocId,Boolean FinalFlag) {

    TimesheetDocumentDetailsNew newDoc = new TimesheetDocumentDetailsNew();

    // newDoc.setDocId(doc.getDocId());
    newDoc.setTimesheetId(doc.getTimesheetId());
    newDoc.setProjectId(projectId);
    newDoc.setFileUrl(uniqueFileName);
    newDoc.setDocName(doc.getDocName());
    newDoc.setActive(doc.getActive());
    newDoc.setFinalFlag(FinalFlag); 
    newDoc.setMimeTypeId(
            timesheetDocumentServiceNew.getMimeTypeId(doc.getDocMimeType(), doc.getDocName()));
    if(FinalFlag){
        newDoc.setClientApprovalStatusId(2);

    }
    else{
        newDoc.setClientApprovalStatusId(1);
    }
   newDoc.setCreatedOn(doc.getCreatedOn() != null ? doc.getCreatedOn() : null);
    // newDoc.setCreatedOn(LocalDateTime.now());
    newDoc.setCreatedBy(doc.getCreatedBy() != null ? doc.getCreatedBy() : doc.getEmpId());
    newDoc.setUpdatedBy(doc.getUpdatedBy());
    newDoc.setUpdatedOn(doc.getUpdatedOn());
    newDoc.setBulkApprovedDocId(bulkApprovedDocId); 
    return newDoc;
}

public FinalDocumentNew buildFinalDocumentNewFromDto(
        TimesheetDocumentMetaDto doc,
        Integer projectId,
        String uniqueFileName) {

    FinalDocumentNew finalDoc = new FinalDocumentNew();

    finalDoc.setDocName(doc.getDocName());
    finalDoc.setProjectId(projectId);
    finalDoc.setFileUrl(uniqueFileName);
    finalDoc.setMimeTypeId(
    		timesheetDocumentServiceNew.getMimeTypeId(doc.getDocMimeType(), doc.getDocName()));
    finalDoc.setCreatedOn(doc.getCreatedOn() != null ? doc.getCreatedOn() : null);
    // finalDoc.setCreatedOn(LocalDateTime.now());
    finalDoc.setCreatedBy(doc.getCreatedBy() != null ? doc.getCreatedBy() : doc.getEmpId());
    finalDoc.setUpdatedBy(doc.getUpdatedBy());
    finalDoc.setUpdatedOn(doc.getUpdatedOn());
    finalDoc.setPrevDocId(doc.getDocId());

    return finalDoc;
}


@Transactional(propagation = Propagation.REQUIRES_NEW)
public FinalDocumentNew saveFinalDoc(FinalDocumentNew finalDoc,MigratedDoc migratedDoc) throws Exception {
    FinalDocumentNew saved = finalDocumentNewRepository.save(finalDoc);
    entityManager.flush();
    migratedDocRepository.save(migratedDoc);
    return saved;
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveTimesheetDocBatch(List<TimesheetDocumentDetailsNew> batch,List<MigratedDoc> migratedDocs) throws Exception {
    timesheetDocumentDetailsNewRepository.saveAll(batch);
    migratedDocRepository.saveAll(migratedDocs);

}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveFailedRecords(List<TempFailedDoc> failedDocs) {
    if (failedDocs == null || failedDocs.isEmpty()) return;
    tempFailedDocRepository.saveAll(failedDocs);
}

}
