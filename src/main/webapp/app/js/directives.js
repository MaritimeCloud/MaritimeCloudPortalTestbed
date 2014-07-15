'use strict';

/* Directives */

angular.module('iamDirectives', []).directive('mcpFocusMe', function($timeout, $parse) {
  // see http://stackoverflow.com/questions/14833326/how-to-set-focus-in-angularjs
  return {
    //scope: true,   // optionally create a child scope
    link: function(scope, element, attrs) {
      var model = $parse(attrs.mcpFocusMe);
      scope.$watch(model, function(value) {
        if (value === true) {
          $timeout(function() {
            element[0].focus();
          });
        }
      });
      // to address @blesh's comment, set attribute value to 'false'
      // on blur event:
      element.bind('blur', function() {
        scope.$apply(model.assign(scope, false));
      });
    }
  };
});

//.directive('mcp-focus', function($timeout, $rootScope) {
//  return {
//    restrict: 'A',
//    scope: {
//      focusValue: "=syncFocusWith"
//    },
//    link: function($scope, $element, attrs) {
//      $scope.$watch("focusValue", function(currentValue, previousValue) {
//        if (currentValue === true && !previousValue) {
//          $element[0].focus();
//        } else if (currentValue === false && previousValue) {
//          $element[0].blur();
//        }
//      })
//    }
//  }
//});
//    .directive('mcp-focus', function() {
//        console.log("XXXXXXXXXXXXXXXXXXXX test", element)
//      return function(scope, element, attrs) {
//        console.log("XXXXXXXXXXXXXXXXXXXX test", element)
//        element[0].focus();
//      };
//    });

angular.module('iamDirectives.ui.bootstrap', ['ui.bootstrap.transition'])

    .directive('minify', ['$transition', function($transition) {

        return {
          link: function(scope, element, attrs) {

            var initialAnimSkip = true;
            var currentTransition;

            function doTransition(change) {
              var newTransition = $transition(element, change);
              if (currentTransition) {
                currentTransition.cancel();
              }
              currentTransition = newTransition;
              newTransition.then(newTransitionDone, newTransitionDone);
              return newTransition;

              function newTransitionDone() {
                // Make sure it's this transition, otherwise, leave it alone.
                if (currentTransition === newTransition) {
                  currentTransition = undefined;
                }
              }
            }

            function expand() {
              console.log("expand");
              if (initialAnimSkip) {
                initialAnimSkip = false;
                expandDone();
              } else {
                element.removeClass('menu-min').addClass('collapsing');
                doTransition({height: element[0].scrollHeight + 'px'}).then(expandDone);
              }
            }

            function expandDone() {
              console.log("expanded");
              element.removeClass('collapsing');
              //element.addClass('collapse in');
              element.css({height: 'auto'});
            }

            function collapse() {
              console.log("collape");
              if (initialAnimSkip) {
                initialAnimSkip = false;
                collapseDone();
                element.css({height: 0});
              } else {
                // CSS transitions don't work with height: auto, so we have to manually change the height to a specific value
                element.css({height: element[0].scrollHeight + 'px'});
                //trigger reflow so a browser realizes that height was updated from auto to a specific value
                var x = element[0].offsetWidth;

                element.removeClass('collapse in').addClass('collapsing');

                doTransition({height: 0}).then(collapseDone);
              }
            }

            function collapseDone() {
              console.log("collapsed");
              element.removeClass('collapsing');
              element.addClass('menu-min');
            }

            //var model = $parse(attrs.minify);
            scope.$watch(attrs.minify, function(shouldCollapse) {
              console.log('shouldCollapse ' + shouldCollapse, shouldCollapse)
              scope.$broadcast('MINIFYEVENT-' + attrs.minify, shouldCollapse);
//          if (shouldCollapse) {
//          } else {
//            expand();
//          }
            });

            //var model = $parse(attrs.minify);
            scope.$on('MINIFYEVENT-' + attrs.minify, function(event, shouldCollapse) {
              console.log('MINIFYEVENT-' + attrs.minify, shouldCollapse)
              if (shouldCollapse) {
                collapse();
              } else {
                expand();
              }
            });
          }
        };
      }])


    .directive('maxify', ['$transition', function($transition) {

        return {
          link: function(scope, element, attrs) {

            var initialAnimSkip = true;

            //var model = $parse(attrs.minify);
            scope.$watch(attrs.minify, function(shouldCollapse) {
              console.log('shouldMaxify XXX ' + shouldCollapse, shouldCollapse)
              scope.$broadcast('MINIFYEVENT-' + attrs.minify, shouldCollapse);
//          if (shouldCollapse) {
//          } else {
//            expand();
//          }
            });

            scope.$on('MINIFYEVENT-' + attrs.minify, function(event, shouldCollapse) {
              console.log('shouldMaxify ' + shouldCollapse, shouldCollapse)
              console.log('Max: MINIFYEVENT-' + attrs.minify, shouldCollapse);
              scope[attrs.minify] = shouldCollapse;
            });
          }
        };
      }])

    ;
