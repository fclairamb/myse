(function () {
	var app = angular.module('myapp', ['tabs']);

	app.controller('MyappController', ['$http', function ($http) {
			this.products = [];

			this.addHint = function (product) {
				product.hints.push(product.nextHint);
				product.nextHint = '';
			}

			var store = this;
			$http.get('/static/gems.json').success(function (data) {
				store.products = data;
			});
		}]);

	app.directive('productTop', function () {
		return {
			restrict: 'E', // E for element, A for attribute
			templateUrl: 'product-top.html'
		};
	});

})();
