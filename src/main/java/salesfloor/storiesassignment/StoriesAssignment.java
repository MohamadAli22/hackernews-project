package salesfloor.storiesassignment;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author MohamadAli Gharaat
 */
public class StoriesAssignment {

    static final int NUMBER_OF_STORIES_TO_GET = 30;
    static final int NUMBER_OF_TOP_COMMENTS_TO_OUTPUT = 10;
    static final String API_COMMENT_TYPE = "comment";
    static Vector<Story> all_top_stories;

    // Hashtable is used because it is Thread-safe. 
    static Hashtable<String, Integer> all_commenter_count_map; //stores comment count for all stories
    static Hashtable<Double, Hashtable<String, Integer>> storyid_commenter_count_map; //stores comment count per story
    // This approach uses more memory (obviously it has duplicates) but is faster.

    public static void main(String[] args) {
        all_top_stories = new Vector<>(NUMBER_OF_STORIES_TO_GET);
        all_commenter_count_map = new Hashtable<>();
        storyid_commenter_count_map = new Hashtable<>();

        OkHttpClient client = MyOkHttpClient.getInstance();

        Request request = new Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/topstories.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    Gson gson = new Gson();
                    String jsonData = response.body().string();
                    List gson_res = gson.fromJson(jsonData, List.class);

                    for (int i = 0; i < Math.min(gson_res.size(), NUMBER_OF_STORIES_TO_GET); i++) {
                        Double item = (Double) gson_res.get(i);
                        getStoryDetails(item);
                    }

                    /* thread to see when network fetching is done and 
                     * to calculat statistics after network finished.
                     */
                    new Thread(new storyStatisticAgreegator()).start();
                }
            }
        });
    }

    public static void getStoryDetails(double storyID) {
        OkHttpClient client = MyOkHttpClient.getInstance();
        Request request = new Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/item/" + (int) (storyID) + ".json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Unexpected code " + response);
                } else { // HTTP request is succesful 
                    Gson gson = new Gson();
                    String jsonData = response.body().string();
                    Story story = gson.fromJson(jsonData, Story.class);

                    // getting comment details of all comments of this story
                    story.commenter_count_map = new HashMap<>();
                    if (story.kids != null && !story.kids.isEmpty()) {
                        for (Double kid : story.kids) {
                            getCommentDetails(kid, storyID);
                        }
                    }
                    all_top_stories.add(story);
                }
            }
        });
    }

    //this method gets details of a comment and also call itself for comments of this comment!
    public static void getCommentDetails(double commentID, double StoryID) {
        OkHttpClient client = MyOkHttpClient.getInstance();
        Request request = new Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/item/" + (int) (commentID) + ".json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Unexpected code " + response);
                } else { // HTTP request is succesful 
                    Gson gson = new Gson();
                    String jsonData = response.body().string();
                    Comment comment = gson.fromJson(jsonData, Comment.class);
                    if (comment != null && comment.type.equals(API_COMMENT_TYPE) && comment.by != null) {
                        // increasing comment counter for all stories
                        all_commenter_count_map.putIfAbsent(comment.by, 0);
                        all_commenter_count_map.put(comment.by, 1 + all_commenter_count_map.get(comment.by));

                        // increasing comment counter per story
                        storyid_commenter_count_map.putIfAbsent(StoryID, new Hashtable<>());
                        Hashtable<String, Integer> commenter_count_per_story = (Hashtable<String, Integer>) storyid_commenter_count_map.get(StoryID);
                        commenter_count_per_story.putIfAbsent(comment.by, 0);
                        commenter_count_per_story.put(comment.by, 1 + commenter_count_per_story.get(comment.by));

                        if (comment.kids != null && !comment.kids.isEmpty()) {
                            for (Double kid : comment.kids) {
                                getCommentDetails(kid, StoryID);
                            }
                        }
                    }
                }
            }
        });
    }

    static class storyStatisticAgreegator implements Runnable {
        //checkes every 250 ms if OKHTTP is done with network IO  
        @Override
        public void run() {
            OkHttpClient client = MyOkHttpClient.getInstance();
            long time = System.currentTimeMillis();
            while (true) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    Logger.getLogger(StoriesAssignment.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (client.dispatcher().queuedCallsCount() + client.dispatcher().runningCallsCount() == 0 && !all_commenter_count_map.isEmpty()) {
                    System.out.println("Network fetching Done.");
                    time = System.currentTimeMillis() - time;
                    System.out.println("All commenters count: " + all_commenter_count_map.size());
                    System.out.println("Calculated in: " + time / 1000 + " (Seconds)");

                    printOutput();// prints the output as you wanted

                    break;
                }
            }
        }

        public void printOutput() {
            for (Story story : all_top_stories) {
                System.out.print("| " + story.title + " | ");
                if (!storyid_commenter_count_map.containsKey(story.id)) {
                    System.out.println("No comments found ...");
                    continue;
                }

                Hashtable commenter_count = storyid_commenter_count_map.get(story.id);
                //sorting commenters of this story
                ArrayList<Map.Entry<String, Integer>> commenter_count_sorted = new Utils().SortHashtable(commenter_count);
                
                for (int i = 0; i < Math.min(commenter_count_sorted.size(), NUMBER_OF_TOP_COMMENTS_TO_OUTPUT); i++) {
                    Map.Entry top_story = (Map.Entry) commenter_count_sorted.get(i);

                    String commenter = (String) top_story.getKey();
                    int numOfComment = (int) top_story.getValue();
                    int totalNumOfComment = all_commenter_count_map.get(commenter);
                    System.out.print(commenter + " (" + numOfComment + " for story - " + totalNumOfComment + " total) | ");
                }
            }
        }

        
    }

    // customized OKHTTP Client
    static class MyOkHttpClient {

        private static OkHttpClient myClient = null;

        public static OkHttpClient getInstance() {
            if (myClient == null) {
                Dispatcher dispatcher = new Dispatcher();
                dispatcher.setMaxRequests(4000);
                dispatcher.setMaxRequestsPerHost(1024);
                myClient = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectionPool(new ConnectionPool(4000, 3, TimeUnit.SECONDS))
                        .dispatcher(dispatcher)
                        .build();
            }
            return myClient;
        }
    }

    class Story {

        private double id;
        private String by;
        private double descendants;
        private List<Double> kids;
        private double score;
        private double time;
        private String title;
        private String type;
        private String url;
        HashMap<Double, Integer> commenter_count_map;

        @Override
        public String toString() {
            return Double.toString(id);
        }
    }

    class Comment {

        double id;
        String by;
        String type;
        private List<Double> kids;

        @Override
        public String toString() {
            return Double.toString(id);
        }
    }

}
