package org.example.redpic.service.impl;
import org.example.redpic.service.RagSearchService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagSearchServiceImpl implements RagSearchService {
    private final VectorStore heroesVectorStore;
    private final VectorStore sitesVectorStore;
    private final VectorStore storiesVectorStore;
    private static final double SIMILARITY_THRESHOLD = 0.48                             ;
    public RagSearchServiceImpl(@Qualifier("heroesVectorStore") VectorStore heroesVectorStore,
                                @Qualifier("sitesVectorStore") VectorStore sitesVectorStore,
                                @Qualifier("storiesVectorStore") VectorStore storiesVectorStore) {
        this.heroesVectorStore = heroesVectorStore;
        this.sitesVectorStore = sitesVectorStore;
        this.storiesVectorStore = storiesVectorStore;

    }

    @Override
    public String searchDocuments(String query, List<String> intentList, boolean fallback) {
        int totalTopK = 6;
        System.out.println("\n=================== 🚦 检索路由日志 ===================");
        System.out.println("重写后检索词：" + query + " | 大模型意图：" + intentList + " | 是否降级：" + fallback);

        boolean isAll = intentList.contains("all");
        boolean hitHeroes = intentList.contains("heroes");
        boolean hitSites = intentList.contains("sites");
        boolean hitStories = intentList.contains("stories");

        if (fallback || isAll) {
            List<String> heroKeywords = List.of(
                    "谁", "将军", "将领", "元帅", "上将", "中将", "少将", "开国", "英雄", "人物", "烈士", "同志",
                    "生平", "简历", "履历", "事迹", "传记", "出身", "出生", "籍贯", "老家", "故乡",
                    "参军", "入伍", "入党", "职务", "军衔", "官", "贡献", "评价", "为人"
            );

            List<String> siteKeywords = List.of(
                    "遗址", "旧址", "遗迹", "地点", "地方", "哪里", "在哪", "什么地儿", "位置", "坐标",
                    "红井", "礼堂", "渡口", "故居", "纪念馆", "博物馆", "建筑", "房子", "村庄", "县城",
                    "苏区", "基地", "根据地", "长征出发地"
            );

            List<String> storyKeywords = List.of(
                    "故事", "经过", "经历", "事件", "传奇", "战斗", "战役", "会议", "长征", "历史", "背景",
                    "怎么", "如何", "发生", "做了什么", "干什么", "参军", "托孤", "写过", "说过", "名言",
                    "精神", "意义", "原因", "为什么"
            );

            hitHeroes = heroKeywords.stream().anyMatch(query::contains);
            hitSites = siteKeywords.stream().anyMatch(query::contains);
            hitStories = storyKeywords.stream().anyMatch(query::contains);
        }

        int hitCount = 0;
        if(hitHeroes){
            hitCount++;
        }
        if(hitSites){
            hitCount++;
        }
        if(hitStories){
            hitCount++;
        }
        int qHeroes=1;
        int qSites=1;
        int qStories=1;
        if(hitCount==0||hitCount==3){
            int average = totalTopK/3;
            int remainder = totalTopK%3;
            qHeroes = average+(remainder>0?1:0);
            qSites = average+(remainder>1?1:0);
            qStories = average;
        }else if(hitCount==1){
            int maxTopk=totalTopK-2;
            if(hitHeroes){
                qHeroes=maxTopk;
            }
            if(hitSites){
                qSites=maxTopk;
            }
            if(hitStories){
                qStories=maxTopk;
            }
        }else if(hitCount==2){
            int remainderTopk=totalTopK-1;
            int arg= remainderTopk/2;
            int remainder=remainderTopk%2;
            if(!hitStories){
                qHeroes = arg;
                qSites = arg+remainder;

            }
            else if(!hitSites){
                qStories=arg;
                qHeroes=arg+remainder;
            }
            else{
                qSites=arg;
                qStories=arg+remainder;
            }

        }
        System.out.println("\n=================== \uD83D\uDEA6 检索路由日志 ===================");
        System.out.println("用户问题："+query);
        System.out.println("意图识别->将领："+hitHeroes+"->遗址："+hitSites+"->故事："+hitStories);
        System.out.println("动态配额分配->将领："+qHeroes+"->遗址："+qSites+"->故事："+qStories);
        System.out.println("====================================");

       List<Document> docs = new ArrayList<>();
       docs.addAll(heroesVectorStore.similaritySearch(SearchRequest.builder().query(query).topK(qHeroes).similarityThreshold(SIMILARITY_THRESHOLD).build()));
       docs.addAll(sitesVectorStore.similaritySearch(SearchRequest.builder().query(query).topK(qSites).similarityThreshold(SIMILARITY_THRESHOLD).build()));
       docs.addAll(storiesVectorStore.similaritySearch(SearchRequest.builder().query(query).topK(qStories).similarityThreshold(SIMILARITY_THRESHOLD).build()));

        docs.forEach(d -> {
            String t = d.getText();
            String preview = t.length() > 30 ? t.substring(0, 30) : t;
            System.out.println("📄 score=" + d.getScore() + " | " + preview.replace("\n", " ") + "...");
        });

       if (docs.isEmpty()) {
           System.out.println("⚠️ 警告：未检索到任何相关文档，触发防幻觉兜底！");
           return "";
       }
        List<String> distinctTexts=docs.stream().map(Document::getText)
               .distinct()
               .collect(Collectors.toList());
        System.out.println("✂️ 去重后喂给大模型的文档数：" + distinctTexts.size());
        System.out.println("=======================================================\n");
        return String.join("\n", distinctTexts);

    }
}
