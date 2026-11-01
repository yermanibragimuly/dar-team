package com.example.demo.service;

import com.example.demo.dto.NewsDTO;
import com.example.demo.entity.News;
import com.example.demo.repository.NewsRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;
    private final MinioService minioService;

    public NewsService(MinioService minioService) {
        this.minioService = minioService;
    }

    // Получить все новости
    public Page<News> getAllNews(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"date"));

        return newsRepository.findAll(pageable);
    }

    // Создать новость
    public News createNews(News news) {
        return newsRepository.save(news);
    }

    public NewsDTO updateNews(Long id, News updatedData) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found with id: " + id));

        // копируем все совпадающие поля, кроме тех, что не надо трогать
        BeanUtils.copyProperties(updatedData, news, "id", "achievements");

        News saved = newsRepository.save(news);
        return mapToDTO(saved);
    }

    public NewsDTO deleteNews(Long id) {
        News news =  newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // 1. Удаляем картинку из MinIO
        minioService.deleteFileByUrl(news.getImg());

        newsRepository.delete(news);

        return  mapToDTO(news);
    }

    public NewsDTO deleteNewsImage(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // 1. Физически удаляем файл из хранилища
        if (news.getImg() != null && !news.getImg().isEmpty()) {
            minioService.deleteFileByUrl(news.getImg());
        }

        // 2. Обнуляем ссылку в БД
        news.setImg(null);
        News saved = newsRepository.save(news);

        return mapToDTO(saved);
    }

    public NewsDTO getnews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));
        return mapToDTO(news);
    }

    private NewsDTO mapToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        BeanUtils.copyProperties(news, dto);
        return dto;
    }
}
