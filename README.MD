# navigation3-browser

This miniature library provides integration between [Androidx.Navigation3](https://developer.android.com/guide/navigation/navigation-3) and [Browser History](https://developer.mozilla.org/en-US/docs/Web/API/History_API)!  

---

To better understand how it works and what API exists,  
watch the following video and answer:   
**what screen do you expect to see when you click the back button in the browser?**

![](./media/g1.gif)

### Answers
**Return to the page you just closed `[PROFILE 2]`**
<details>
<summary>Chronological Navigation</summary>

![](./media/g2.gif)

We will call this type of navigation **chronological**.  
This is how standard web applications, such as Wikipedia or GitHub, usually behave.  
The idea is that the browser history stores a stack of all screen changes in chronological order.  
This allows you to move back and forth between these states.  
This can be very convenient, but it allows for unplanned transitions within the application.

### Pros:
 - close to default desktop web browser behavior
 - supports navigation through the browser address bar
 - there is back and forth navigation between saved states

### Cons:
 - bound to only one navigation backstack
 - may provide unusual navigation behavior (e.g. open closed dialogs by back button)

</details>


**Return to `[kotlinlang.org]`!**
<details>
<summary>Hierarchical Navigation</summary>

![](./media/g3.gif)

We will call this type of navigation **hierarchical**.  
That is, it is related to the current hierarchy of screens in the application.  
This is how regular applications in the system usually behave.  
From the main screen, you can only exit the application completely.  
This type of navigation does not imply “forward” navigation,  
but when you click back, you can perform some action,  
such as displaying a dialog asking if the user wants to leave the application.  
**This type of navigation is also more convenient to use from a mobile browser.**

### Pros:
 - supports any backstack nesting
 - behaves like a regular mobile application
 - does not allow unplanned navigation

### Cons:
 - does not support navigation through the browser address bar
 - does not support forward navigation

</details>

## How to use
`//todo`