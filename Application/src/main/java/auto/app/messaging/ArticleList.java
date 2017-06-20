package auto.app.messaging;

import java.io.Serializable;
import java.util.List;

public class ArticleList implements Serializable {
    public List<Article> data;

    public ArticleList(List<Article> data) {
        this.data = data;
    }

    public Article getNext() {
        if(data.size() > 0)
            return data.remove(0);
        return null;
    }
}
