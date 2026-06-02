export function pageContent(response) {
  return Array.isArray(response) ? response : response?.content || [];
}

export function pageMeta(response) {
  if (Array.isArray(response)) {
    return {
      content: response,
      page: 0,
      size: response.length,
      totalElements: response.length,
      totalPages: response.length ? 1 : 0,
      first: true,
      last: true
    };
  }

  return {
    content: response?.content || [],
    page: response?.page || 0,
    size: response?.size || response?.content?.length || 0,
    totalElements: response?.totalElements || response?.content?.length || 0,
    totalPages: response?.totalPages || 0,
    first: response?.first ?? true,
    last: response?.last ?? true
  };
}
