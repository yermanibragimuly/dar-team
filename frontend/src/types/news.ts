export interface News {
  id: number;
  title: string;
  content: string;
  img: string;
  date: string; // можно заменить на Date, если будешь парсить дату
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}