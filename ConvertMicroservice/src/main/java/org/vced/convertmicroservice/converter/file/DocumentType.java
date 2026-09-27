package org.vced.convertmicroservice.converter.file;

public enum DocumentType {
    // на момент 06.06.25 в папке ИП РИ ОИ СТО, находятся следующие форматы документов:
    // {DOCX=4, dotx=1, xlsx=14, rtf=1, vsd=2, lnk=5, dot=6, docx=169, pdf=54, xlsm=1, doc=44, docm=1, db=21, xls=3}
    // формат=количество_документов_этого_формата
    DOCX,
    DOC,
    XLSX,
    PDF
}