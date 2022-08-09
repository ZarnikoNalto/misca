package msifeed.misca.tags;

import java.util.LinkedList;
import java.util.List;

public class ItemTagsData {
    public ItemTags speed;
    public ItemTags hands;
    public ItemTags shield;
    public List<ItemTags> skills  = new LinkedList<>();
    public List<ItemTags> other = new LinkedList<>();
}
