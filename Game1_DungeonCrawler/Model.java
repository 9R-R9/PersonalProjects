import java.util.ArrayList;
import java.util.Iterator;

public class Model{

    private ArrayList<Sprite> sprites;
    private Link link;
    private Sprite sprite;
    private boolean Up; 
    private boolean Down;
    private boolean Left;
    private boolean Right;
    private int lastX;
    private int lastY;
    private int mapX = 0; 
    private int mapY = 0;
    private ArrayList<Sprite> itemsICanAdd;
    private int itemNum = 0;
    private int rupeeCount = 0;

    public Model(){
        this.sprites = new ArrayList<Sprite>(); 
        link = new Link(200, 200);
        this.sprites.add(link);
        this.itemsICanAdd = new ArrayList<Sprite>();
        this.itemsICanAdd.add(new Tree(0, 0));
        this.itemsICanAdd.add(new TreasureChests(0, 0));
    }
    
    public void update(int screenWidth, int screenHeight){  
        
        //set all values correctly so Link can do his thing
        link.setInputs(this.Up, this.Down, this.Left, this.Right, this.sprites);
        
        //calls update for every sprite
        for(Sprite s : sprites){
            s.update();
        }

        //continous collision check
        Iterator<Sprite> it1 = sprites.iterator();
        while(it1.hasNext()){
            Sprite s1 = it1.next();
            boolean s1Valid = s1.update();
            if(!s1Valid){
                it1.remove(); 
                continue;  
            }
            Iterator<Sprite> it2 = sprites.iterator();
            while(it2.hasNext()){
                Sprite s2 = it2.next();
                if(s1 == s2){
                    continue;
                }
                //is s1 and s2 colliding
                if(Sprite.doesCollide(s1, s2)){
                    handleCollision(s1, s2);
                }
            }
        }

        //changing rooms
        if(link.getY() >= this.getMapPosY() + screenHeight){
			this.goDown();
            link.setPosition(link.getX(), this.mapY + 1);
		}else if(link.getY() < this.getMapPosY()){
			this.goUp();
            link.setPosition(link.getX(), this.mapY + screenHeight - link.getH() - 1);
		}
		if(link.getX() >= this.getMapPosX() + screenWidth){
			this.goRight();
            link.setPosition(this.mapX + 1, link.getY());
		}else if(link.getX() < this.getMapPosX()){
			this.goLeft();
            link.setPosition(this.mapX + screenWidth - link.getW() - 1, link.getY());
		}
    }

    //collect those gems
    public int getRupeeCount() {
        return this.rupeeCount;
    }
    private void collectRupee() {
        this.rupeeCount++;
        System.out.println("Rupee collected! Total: " + this.rupeeCount);
    }

    public Json marshal(){
        Json ob = Json.newObject();
        Json list = Json.newList();
        for(Sprite s : this.sprites){
            if(s.saved()){
                list.add(s.marshal());
            }
        }
        ob.add("sprites", list);
        return ob;
    }
    public void unmarshal(Json ob){
        clearMap();
        Json tempList = ob.get("sprites");
        //String type = "";
        for(int i = 0; i < tempList.size(); i++){
            //Json spriteOb = tempList.get(i); <-- these lol
            //Json typeJson = spriteOb("type"); <-- these lol
            //type = typeJson.asString(); <-- these lol
            /*lol i had like 4 more variables that did this but seeing
            you can just keep adding . and adding more stuff 
            this works and is funny, not doing any more though*/
            if(tempList.get(i).get("type").asString().equals("tree")){
                this.sprites.add(new Tree(tempList.get(i)));
            }else if(tempList.get(i).get("type").asString().equals("chest")){
                this.sprites.add(new TreasureChests(tempList.get(i)));
            }
        }
    }

    public void clearMap(){
        //only removes sprites that are saved
        Iterator<Sprite> it = sprites.iterator();
        while(it.hasNext()){
            Sprite s = it.next();
            //tree returns true, link returns false
            if(s.saved()){
                it.remove();
            }
        }
    }

    public void addSprites(int x, int y){
        //check type
        Sprite typeToAdd = itemsICanAdd.get(itemNum);
        Sprite newSprite = null;
        
        if(typeToAdd.isTree()){
            int gridW = Tree.TREE_WIDTH; int gridH = Tree.TREE_HEIGHT;
            newSprite = new Tree(Math.floorDiv(x, gridW) * gridW, Math.floorDiv(y, gridH) * gridH);
        }else if(typeToAdd.isChest()){
            newSprite = new TreasureChests(x, y);
        }
        //overlap with existing obstacles
        for(Sprite existing : sprites){
            if(Sprite.doesCollide(newSprite, existing)){
                System.out.println("Cannot place: overlaps existing obstacle");
                return;
            }
        }
        //add sprite if no bosticle
        this.sprites.add(newSprite);
        if(newSprite.isObstacle() && Sprite.doesCollide(link, newSprite)){
            handleCollision(link, newSprite);
        }
    }
    public void removeSprites(int x, int y){
        Sprite typeToRemove = getItemIAmAdding();
        if(typeToRemove == null){
            return;
        }
        for(int i = 0; i < this.sprites.size(); i++){
            Sprite sprite = this.sprites.get(i);
            if(x >= sprite.getX() && 
               x < sprite.getX() + sprite.getW() &&
               y >= sprite.getY() && 
               y < sprite.getY() + sprite.getH() && 
               ((typeToRemove.isTree() && sprite.isTree()) ||
               (typeToRemove.isChest() && sprite.isChest()))){
               if(sprite.saved()){ 
                    this.sprites.remove(i);
                    //break here to remove 1 sprite and not all sprites
                    break;
               }
            }
        }
    }

    public void spawnBoomerang(){
        int dir = link.getLastDirection(); 
        int speed = 20;
        int dx = 0, dy = 0;

        //direction
        if(dir == 0){dy = speed;} 
        if(dir == 1){dx = -speed;} 
        if(dir == 2){dx = speed;}  
        if(dir == 3){dy = -speed;}

        //spawn position (center of Link accounts for boomerang size)
        int spawnX = link.getX() + link.getW() / 2 - Boomerang.BOOMERANG_WIDTH / 2;
        int spawnY = link.getY() + link.getH() / 2 - Boomerang.BOOMERANG_HEIGHT / 2;

        sprites.add(new Boomerang(spawnX, spawnY, dx, dy));
    }
    
    //Getter
    public ArrayList<Sprite> getSprites(){
        return this.sprites;
    }

    public void movementDirectionToF(boolean up, boolean down, boolean left, boolean right){
        this.Up = up;
        this.Down = down;
        this.Left = left;
        this.Right = right;
    }

    public Sprite getItemIAmAdding(){
        if(itemsICanAdd == null || itemsICanAdd.isEmpty()){
            return null;
        }
        if(itemNum < 0 || itemNum >= itemsICanAdd.size()){
            itemNum = 0;
        }
        return itemsICanAdd.get(itemNum);
    }
    public void cycleAddItem(){
        if(itemsICanAdd != null && !itemsICanAdd.isEmpty()){
             itemNum = (itemNum + 1) % itemsICanAdd.size();
        }
    }

    private void handleCollision(Sprite s1, Sprite s2){
        TreasureChests chestObj = null;
        boolean isCollectible = false;
        //link and tree collision
        if(s1.isLink() && s2.isTree()){
            collisionLogic(s1, s2);
        }else if(s2.isLink() && s1.isTree()){
            collisionLogic(s2, s1);
        }
        //link and chest/rupee collison
        //collects rupee after a short delay
        if(s1.isLink() && s2.isChest()){
            chestObj = (TreasureChests) s2;
            isCollectible = chestObj.getIsOpen() && chestObj.getOpeningTimer() <= 0;
            collisionLogic(s1, s2);
            ((TreasureChests)s2).interact(s1);
            if(isCollectible){collectRupee();}
        }else if(s2.isLink() && s1.isChest()){
            chestObj = (TreasureChests) s1;
            isCollectible = chestObj.getIsOpen() && chestObj.getOpeningTimer() <= 0;
            collisionLogic(s2, s1);
            ((TreasureChests)s1).interact(s2);
            collectRupee();
            if(isCollectible){collectRupee();}
        }
        //boomerang and tree collison
        if(s1.isBoomerang() && s2.isTree()){
            collisionLogic(s1, s2);
            ((Boomerang)s1).disappear();
        }else if(s2.isBoomerang() && s1.isTree()){
            collisionLogic(s2, s1);
            ((Boomerang)s2).disappear();
        }
        //boomerang and chest/rupee collison
        //made sure boomerang only interacts with chest and not rupee
        if(s1.isBoomerang() && s2.isChest()){
            chestObj = (TreasureChests) s2;
            isCollectible = chestObj.getIsOpen() && chestObj.getOpeningTimer() <= 0;
            if(isCollectible){
            }else{
                collisionLogic(s1, s2);
                ((Boomerang)s1).disappear();
                ((TreasureChests)s2).interact(s1);
            }
        }else if(s2.isBoomerang() && s1.isChest()){
            chestObj = (TreasureChests) s1;
            isCollectible = chestObj.getIsOpen() && chestObj.getOpeningTimer() <= 0;
            if(isCollectible){
            }else{
                collisionLogic(s2, s1);
                ((Boomerang)s2).disappear();
                ((TreasureChests)s1).interact(s2);
            }
        }
    }
    private void collisionLogic(Sprite mainItem, Sprite obstacle){
        //overlaps on all four sides
        int overlapRight = (mainItem.getX() + mainItem.getW()) - obstacle.getX();
        int overlapLeft = (obstacle.getX() + obstacle.getW()) - mainItem.getX();
        int overlapDown = (mainItem.getY() + mainItem.getH()) - obstacle.getY();
        int overlapUp = (obstacle.getY() + obstacle.getH()) - mainItem.getY();

        //find the minimum overlap
        int minOverlap = Integer.MAX_VALUE;
        if(overlapRight > 0 && overlapRight < minOverlap){ 
            minOverlap = overlapRight;
        }
        if(overlapLeft > 0 && overlapLeft < minOverlap){   
            minOverlap = overlapLeft;
        }
        if(overlapDown > 0 && overlapDown < minOverlap){ 
            minOverlap = overlapDown;
        }
        if(overlapUp > 0 && overlapUp < minOverlap){      
            minOverlap = overlapUp;
        }

        //push mainItem item out in the direction of the minimum overlap
        if(minOverlap == overlapRight){
            mainItem.setPosition(mainItem.getX() - overlapRight, mainItem.getY());
        }else if(minOverlap == overlapLeft){
            mainItem.setPosition(mainItem.getX() + overlapLeft, mainItem.getY());
        }else if(minOverlap == overlapDown){
            mainItem.setPosition(mainItem.getX(), mainItem.getY() - overlapDown);
        }else if(minOverlap == overlapUp){
            mainItem.setPosition(mainItem.getX(), mainItem.getY() + overlapUp);
        }
    }

    //map positions
    public int getMapPosX(){
		return mapX;
	}
	public int getMapPosY(){
		return mapY;
	}

    //go places
    public void goUp(){
        if(mapY > -500){
            mapY -= 500;
        }else{
            return;
        }
    }
    public void goDown(){
        if(mapY < 500){
            mapY += 500;
        }else{
            return;
        }
    }
    public void goLeft(){
        if(mapX > -1000){
            mapX -= 1000;
        }else{
            return;
        }
    }
    public void goRight(){
        if(mapX < 1000){
            mapX += 1000;
        }else{
            return;
        }
    }
}
