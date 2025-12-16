package edu.univ.erp.service.student;

import java.util.List;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.common.PagedResult;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.GradeView;
import edu.univ.erp.domain.TimetableEntry;
import edu.univ.erp.domain.TranscriptRecord;

public interface StudentService {

    PagedResult<SectionRow> browseCatalog(AuthenticatedUser user, int page, int pageSize);

    List<SectionRow> myRegistrations(AuthenticatedUser user);

    List<TimetableEntry> timetable(AuthenticatedUser user);

    List<GradeView> gradeReport(AuthenticatedUser user);

    byte[] exportTranscriptCsv(AuthenticatedUser user);

    byte[] exportTranscriptPdf(AuthenticatedUser user);

    ApiResponse<Void> register(AuthenticatedUser user, long sectionId);

    ApiResponse<Void> drop(AuthenticatedUser user, long sectionId);

    List<TranscriptRecord> transcriptRecords(AuthenticatedUser user);
}

