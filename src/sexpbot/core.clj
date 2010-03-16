(ns sexpbot.core
  (:use [sexpbot.plugins.utils]
	[sexpbot.plugins.8ball]
	[sexpbot.respond]
	[clojure.contrib.str-utils :only [re-split]])
  (:import (org.jibble.pircbot PircBot)))

(def botconfig 
     (ref {:prepend \$
	   :server "irc.freenode.net"
	   :channel "#acidrayne"}))

(defn wall-hack-method [class-name name- params obj & args]
  (-> class-name (.getDeclaredMethod (name name-) (into-array Class params))
    (doto (.setAccessible true))
    (.invoke obj (into-array Object args))))

(defn split-args [s] (let [[command & args] (re-split #" " s)]
		       {:command command
			:args args}))

(defn unload [{:keys [bot channel args]}]
  (remove-ns (symbol (first args)))
  (.sendMessage bot channel (str (first args) " unloaded.")))

(defn make-bot [] 
  (let [bot (proxy [PircBot] []
	      (onMessage 
	       [chan send login host mess]
	       (if (= (first mess) (@botconfig :prepend))
		 (respond (merge (split-args (apply str (rest mess)))
				 {:bot this 
				  :sender send 
				  :channel chan 
				  :login login 
				  :host host})))))]
    (wall-hack-method PircBot :setName [String] bot "sexpbot")
    (doto bot
      (.setVerbose true)
      (.connect "irc.freenode.net")
      (.joinChannel "#()"))
    (dosync (alter botconfig merge {:bot bot}))))

(make-bot)
