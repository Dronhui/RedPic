package org.example.redpic.service;

import java.util.List;

public interface RagSearchService {
 public String searchDocuments(String query, List<String> intentList, boolean fallback);
}
