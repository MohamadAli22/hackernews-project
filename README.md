# hackernews-project
A project done to retrieve top N stories from the hacker news API and show some statistics about them as fast as possible. 
I assumed that time is more important than RAM.

## How it works?
- It calls the Hackernews API to retrieve the top 30 story IDs. 
- Having story IDs, creates 1 thread for each story ID to gather story details (including comment IDs)
- then it will call API for each comment ID to get the *Commenter Name* and to see whether the comment itself has more comments or not.
- if it had comments it will also retrieve those comments too.
- It uses HashTable for saving each commenter's comment counts because it is thread-safe.
- There are two hash tables to store the statistics of the commenters. First, for counting the number of comments a commenter had in all stories. Second, for counting the number of comments a commenter had per story. This design uses more RAM but is faster even when the number of stories goes up.
- Then we sort the hashtable values and bring the top N=10 commenters in the output.
We could have used (always sorted) Hashtree instead of Hashtable. It can do the sorting at the insertion time, but since it was not thread-safe we decided to break it into two steps.

## Time complexity?
Let's say N is the number of comments.
Two hashtables insertion time complexity is O(N).
Two hashtables sorting time complexity is O(N Log(N)).
And then there is a small process of getting the top 10 commenters for every 30 stories  O(10*30) and print statistics.

## Results
It uses the OKHTTP library to handle API calls. In my evaluation, It could gather ALL comments of 30 top stories in *3-5 Seconds*.
By "ALL comments", I mean even comments of comments will be gathered. and then the desired statistics will be shown in the output.
