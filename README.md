# schuko

showoff-style markdown->html presentation software, in clojure(script)

The name is from Wikipedia-assisted word association starting with "Powerpoint": 
> "Schuko" /ˈʃuːkoʊ/ is the colloquial name for a system of AC power plugs and sockets that is defined as "CEE 7/4".

Early days yet: prepare to be unimpressed.

## Usage

```
$ lein deps
$ lein compile
$ lein cljsbuild once
$ lein uberjar
$ java -jar target/schuko-0.1.0-SNAPSHOT-standalone.jar input.md output.html
```

where, as you may guess from the names, the first argument is a
Markdown-formatted input file and the second is the HTML file you want
it to generate.  This file will have all the necessary JS and CSS
embedded within it.

Now open `output.html` in a web browser.  In Unix tradition, the
`Space` bar will go to the next slide and the `Backspace` key will
take you to the previous slide

## License

Copyright © 2013 Daniel Barlow

Distributed under the Eclipse Public License, the same as Clojure.
