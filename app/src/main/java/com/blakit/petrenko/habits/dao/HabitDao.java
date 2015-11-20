package com.blakit.petrenko.habits.dao;

import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Article;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.VideoItem;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * Created by user_And on 31.07.2015.
 */
public class HabitDao {
    private Dao<Habit, Integer> habitDao;
    private Dao<Action, Integer> actionDao;
    private Dao<VideoItem, Integer> videoDao;
    private Dao<Article, Integer> articleDao;

    public HabitDao(HabitsDBOpenHelper habitsDBOpenHelper) throws SQLException {
        habitDao = habitsDBOpenHelper.getDao(Habit.class);
        actionDao = habitsDBOpenHelper.getDao(Action.class);
        videoDao = habitsDBOpenHelper.getDao(VideoItem.class);
        articleDao = habitsDBOpenHelper.getDao(Article.class);
    }


    public void createOrUpdate(Habit habit) throws SQLException {
        habitDao.create(habit);
        for (Action action: habit.getActions()) {
            action.setHabit(habit);
            actionDao.create(action);
        }
        for (VideoItem video: habit.getRelatedVideoItems()) {
            video.setHabit(habit);
            videoDao.create(video);
        }
        for (Article article: habit.getRelatedArticles()) {
            article.setHabit(habit);
            articleDao.create(article);
        }
    }

    public Dao<Habit, Integer> getDao() {
        return habitDao;
    }
}
