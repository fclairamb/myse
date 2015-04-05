(function () {
	var app = angular.module('myse', []);

	app.controller('NavigationController', function () {
		this.current = 'search';
		this.select = function (page) {
			this.current = page;
		};
		this.isSelected = function (page) {
			//console.log(this.current + ' / ' + page);
			return this.current === page;
		};
		this.class = function (page) {
			return {
				active: this.isSelected(page)
			};
		};
	});

	app.controller('Version', ['$http', function ($http) {
			this.props = {};
			var ctrl = this;
			$http.get('/rest/version').success(function (data) {
				ctrl.props = data;
			});
		}]);

	app.directive('myseSearch', function () {
		return {
			restrict: 'E',
			templateUrl: '/static/search.html',
			controller: ['$http', function ($http) {
					// CONTROLLER CODE
					var ctrl = this;
					this.query = '';
					this.response = {
						results: [],
						error: null
					};
					this.queryChanged = function () {
						$http.post('/rest/search', {'q': this.query}).success(
								function (data) {
									ctrl.response = data;
								}
						);
					};
				}
			],
			controllerAs: 'search'
		};
	});

	app.directive('myseSetup', function () {
		return {
			restrict: 'E',
			templateUrl: '/static/setup.html',
			controller: ['$http', function ($http) {
					var ctrl = this;
				}],
			controllerAs: 'setup'
		};
	});

})();

$('#core').show();
